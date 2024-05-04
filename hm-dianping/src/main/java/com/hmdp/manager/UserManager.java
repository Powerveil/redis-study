package com.hmdp.manager;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.constant.RedisConstants;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @ClassName UserManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/5/4 18:11
 * @Version 1.0
 */
@Component
public class UserManager {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserInfoService userInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Result sendCode(String phone, HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 实现登录功能
        return userService.login(loginForm, session);
    }

    public Result info(Long userId) {
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    public Result queryUserById(Long userId) {
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    public Result sign() {
        // 1.获取当前的登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyy/MM"));
        String key = RedisConstants.USER_SIGN_KEY + userId + keySuffix;
        // 4.获取几年是本月的第几天
        long dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }
}
