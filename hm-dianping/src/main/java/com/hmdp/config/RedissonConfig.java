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


    @Value("${power.redis.port1}")
    private String REDIS_PORT1;
    @Value("${power.redis.port2}")
    private String REDIS_PORT2;
    @Value("${power.redis.port3}")
    private String REDIS_PORT3;

    @Bean
    public RedissonClient redissonClient() {
        String connectionUrl = "redis://" + REDIS_IP + ":" + REDIS_PORT;
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress(connectionUrl).setPassword(REDIS_PASSWORD);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }

    @Bean
    public RedissonClient redissonClient1() {
        String connectionUrl = "redis://" + REDIS_IP + ":" + REDIS_PORT1;
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress(connectionUrl).setPassword(REDIS_PASSWORD);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }


    @Bean
    public RedissonClient redissonClient2() {
        String connectionUrl = "redis://" + REDIS_IP + ":" + REDIS_PORT2;
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress(connectionUrl).setPassword(REDIS_PASSWORD);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }


    @Bean
    public RedissonClient redissonClient3() {
        String connectionUrl = "redis://" + REDIS_IP + ":" + REDIS_PORT3;
        // 配置
        Config config = new Config();
        config.useSingleServer().setAddress(connectionUrl).setPassword(REDIS_PASSWORD);
        // 创建RedissonClient对象
        return Redisson.create(config);
    }
}
