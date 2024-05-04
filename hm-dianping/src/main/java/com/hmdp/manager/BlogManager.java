package com.hmdp.manager;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.constant.RedisConstants;
import com.hmdp.constant.SystemConstants;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.BeanCopyUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName BlogManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/5/2 16:11
 * @Version 1.0
 */
@Component
@Slf4j
public class BlogManager {

    @Autowired
    private IBlogService blogService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IFollowService followService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Result saveBlog(Blog blog) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        blog.setUserId(userId);
        // 2.保存探店博文
        boolean isSuccess = blogService.save(blog);
        if (isSuccess) {
            // 3.查询笔记作者的所有粉丝
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getFollowUserId, userId);
            List<Long> userIdList = followService.list(queryWrapper).stream()
                    .map(Follow::getUserId)
                    .collect(Collectors.toList());
            // 4.推送笔记id给所有粉丝
            for (Long funUserId : userIdList) {
                String key = RedisConstants.FEED_KEY + funUserId;
                stringRedisTemplate.opsForZSet().add(key, blog.getId().toString(), System.currentTimeMillis());
            }
        }
        // 返回id
        return Result.ok(blog.getId());
    }

    public Result likeBlog(Long id) {
        // 1.获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.判断当前登录用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (Objects.isNull(score)) {
            // 3.如果没有点赞，则点赞，并修改点赞数量
            // 3.1.数据库点赞数+1
            boolean isSuccess = blogService.update()
                    .setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的zset集合中 zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已经点赞，则取消点赞，并修改点赞数量
            // 4.1.数据库点赞数-1
            boolean isSuccess = blogService.update()
                    .setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的zset集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return Result.ok();
    }

    public Result queryMyBlog(Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 封装数据
        records.forEach(blog -> {
            // 2.查询blog有关的用户
            this.queryBlogUser(blog);
            // 3.查询blog是否被点赞
            this.isBlogLiked(blog);
        });

        return Result.ok(records);
    }

    public Result queryBlogById(Long id) {
        // 1.查询Blog
        Blog blog = blogService.getById(id);
        if (Objects.isNull(blog)) {
            return Result.fail("博文不存在");
        }
        // 2.查询blog有关的用户
        this.queryBlogUser(blog);
        // 3.查询blog是否被点赞
        this.isBlogLiked(blog);

        return Result.ok(blog);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    private void isBlogLiked(Blog blog) {
        // 1.获取登录用户
        UserDTO user = UserHolder.getUser();
        if (Objects.isNull(user)) {
            return;
        }
        Long userId = user.getId();
        // 2.判断当前登录用户是否已经点赞
        String key = RedisConstants.BLOG_LIKED_KEY + blog.getId().toString();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(Objects.nonNull(score));
    }

    public Result queryBlogLikes(Long id) {
        // 1.查询top的点赞用户 zrange key 0 4
        String key = RedisConstants.BLOG_LIKED_KEY + id;
        // 2.解析出其中的用户id
        Set<String> range = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (Objects.isNull(range) || range.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 3.根据用户id查询用户
        List<Long> ids = range.stream().map(Long::valueOf).collect(Collectors.toList());
        String join = StrUtil.join(",", ids);
        String lastSql = String.format("ORDER BY FIELD(id,%s)", join);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .in(User::getId, ids)
                .last(lastSql);
        List<User> users = userService.list(queryWrapper);
        List<UserDTO> userDTOS = BeanCopyUtils.copyBeanList(users, UserDTO.class);
        // 4.返回用户
        return Result.ok(userDTOS);
    }

    public Result queryBlogByUserId(Integer current, Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    public Result queryBlogByFollow(Long lastId, Integer offset) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        // 2.查询收件箱
        String key = RedisConstants.FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, lastId, offset, 2);
        // 3.非空判断
        if (Objects.isNull(typedTuples) || typedTuples.isEmpty()) {
            return Result.ok();
        }
        // 4.解析数据：blogId, minId（时间戳），offset
        List<Long> blogIdList = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        int os = 1;
        for (ZSetOperations.TypedTuple<String> typedTuple : typedTuples) {
            // 4.1.获取id
            blogIdList.add(Long.valueOf(typedTuple.getValue()));
            // 4.2.获取分数（时间戳）
            long time = typedTuple.getScore().longValue();
            // 4.3.获取offset
            if (time == minTime) {
                os++;
            } else {
                minTime = time;
                os = 1;
            }
        }
        // 5.根据id查询blog
        // todo 带优化
        String join = StrUtil.join(",", blogIdList);
        String lastSql = String.format("ORDER BY FIELD(id,%s)", join);
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Blog::getId, blogIdList)
                .last(lastSql);
        List<Blog> blogs = blogService.list(queryWrapper);
        for (Blog blog : blogs) {
            // 2.查询blog有关的用户
            this.queryBlogUser(blog);
            // 3.查询blog是否被点赞
            this.isBlogLiked(blog);
        }
        ScrollResult result = new ScrollResult(blogs, minTime, os);
        return Result.ok(result);
    }
}
