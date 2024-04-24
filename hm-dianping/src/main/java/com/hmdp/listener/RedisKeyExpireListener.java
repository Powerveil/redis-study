package com.hmdp.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @ClassName RedisKeyExpireListener
 * @Description redis 过期事件监听器
 * @Author Powerveil
 * @Date 2024/4/24 22:53
 * @Version 1.0
 */
@Component
public class RedisKeyExpireListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpireListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 重写 onMessage方法
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 过期的key
        String expireKey = message.toString();
        System.out.println("过期的key：" + expireKey);
        // 根据key,执行自己需要实现的功能。
    }
}
