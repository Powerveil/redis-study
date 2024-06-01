package com.hmdp.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName RedisKeyExpireListener
 * @Description redis 过期事件监听器
 * @Author Powerveil
 * @Date 2024/4/24 22:53
 * @Version 1.0
 */
@Component
public class RedisKeyExpireListener extends KeyExpirationEventMessageListener {
    @Autowired
    private RedisTemplate redisTemplate;

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
//        Map<String, Integer> map = new HashMap<>();
//        if (expireKey.contains("test:order:")) {
//            BoundHashOperations<String, String, Integer> hashOperations = redisTemplate.boundHashOps(expireKey);
//            // 这是key已经被删除，无法获取数据
//            Set<String> keys = hashOperations.keys();
//            for (String key : keys) {
//                Integer value = hashOperations.get(key);
//                map.put(key, value);
//            }
//        }
//        System.out.println(map.toString());
    }
}
