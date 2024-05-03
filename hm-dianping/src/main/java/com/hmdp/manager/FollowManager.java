package com.hmdp.manager;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.BeanCopyUtils;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName FollowManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/5/3 17:19
 * @Version 1.0
 */
@Component
public class FollowManager {
    @Autowired
    private IFollowService followService;

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public Result fellow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key = RedisConstants.FOLLOW_KEY + userId;
        // 1.判断是否关注
        if (BooleanUtil.isTrue(isFollow)) {
            // 2.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = followService.save(follow);
            if (isSuccess) {
                // 把关注用户的id，放入redis的set集合 sadd userId followUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.取消关注
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId, userId)
                        .eq(Follow::getFollowUserId, followUserId);
            boolean isSuccess = followService.remove(queryWrapper);
            if (isSuccess) {
                // 3.把关注用户的id，从redis的set集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        // todo 这里先不判断redis宕机，数据丢失的问题，如果有暂时想法是依靠定时任务
        // 前置判断
//        stringRedisTemplate.opsForSet().
//        stringRedisTemplate.opsForSet()
//
//        Boolean isMember = stringRedisTemplate.opsForSet().isMember(userId.toString(), followUserId.toString());
//        if (BooleanUtil.isTrue(isMember)) {
//            return Result.ok(true);
//        }
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId);
        int count = followService.count(queryWrapper);
        return Result.ok(count > 0);
    }

    public Result followCommons(Long id) {
        // 1.获取当前用户
        Long userId = UserHolder.getUser().getId();
        String key1 = RedisConstants.FOLLOW_KEY + userId;
        String key2 = RedisConstants.FOLLOW_KEY + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if (Objects.isNull(intersect) || intersect.isEmpty()) {
            //无交集
            return Result.ok(Collections.emptyList());
        }
        // 3.解析id集合
        List<Long> userIdList = intersect.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        // 4.查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(User::getId, userIdList);
        List<User> userList = userService.list(queryWrapper);
        List<UserDTO> userDTOS = BeanCopyUtils.copyBeanList(userList, UserDTO.class);
        return Result.ok(userDTOS);
    }
}
