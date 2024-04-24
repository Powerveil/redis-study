package com.hmdp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * @ClassName RedisConfig
 * @Description Redis配置类
 * @Author Powerveil
 * @Date 2024/4/24 22:52
 * @Version 1.0
 * <a href="https://blog.csdn.net/qq_43108153/article/details/132276678">SpringBoot整合Redis监听Key取消超时订单</a>
 */
@Configuration
public class RedisConfig {
    // redis key 过期监听事件
    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

}
