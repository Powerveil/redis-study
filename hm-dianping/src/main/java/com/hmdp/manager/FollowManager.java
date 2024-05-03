package com.hmdp.manager;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public Result fellow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        // 1.判断是否关注
        if (BooleanUtil.isTrue(isFollow)) {
            // 2.关注，新增数据
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            followService.save(follow);
        } else {
            // 3.取消关注
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId, userId)
                        .eq(Follow::getFollowUserId, followUserId);
            followService.remove(queryWrapper);
        }
        return Result.ok();
    }

    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId)
                .eq(Follow::getFollowUserId, followUserId);
        int count = followService.count(queryWrapper);
        return Result.ok(count > 0);
    }
}
