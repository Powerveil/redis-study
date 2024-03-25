package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RedissonConfig
 * @Description Redisson配置类
 * @Author Powerveil
 * @Date 2024/3/25 10:54
 * @Version 1.0
 */
@Configuration
public class RedissonConfig {

    @Value("${power.redis.ip}")
    private String REDIS_IP;
    @Value("${power.redis.port}")
    private String REDIS_PORT;
    @Value("${power.redis.password}")
    private String REDIS_PASSWORD;

    @Bean
    public RedissonClient redissonClient() {
        String connectionUrl = "redis://" + REDIS_IP + ":" + REDIS_PORT;
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress(connectionUrl).setPassword(REDIS_PASSWORD);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
