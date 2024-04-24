package com.hmdp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName RedissonTest
 * @Description Redisson测试类
 * @Author Powerveil
 * @Date 2024/3/25 11:28
 * @Version 1.0
 */
@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedissonClient redissonClient1;

    @Resource
    private RedissonClient redissonClient2;

    @Resource
    private RedissonClient redissonClient3;

    private RLock lock;

    @BeforeEach
    void setUp() {
//        lock = redissonClient.getLock("order");

        RLock lock1 = redissonClient1.getLock("order");
        RLock lock2 = redissonClient2.getLock("order");
        RLock lock3 = redissonClient3.getLock("order");

        // 创建联锁 multiLock
        // 用谁的redissonClient调用都一样，都是new
        lock = redissonClient.getMultiLock(lock1, lock2, lock3);
    }

    @Test
    void method1() throws InterruptedException {
        // 尝试获取锁
        ReentrantLock lock1 = new ReentrantLock();
//        lock1.tryLock()
//        boolean isLock = lock.tryLock(100L, 1000L,TimeUnit.SECONDS);
        boolean isLock = lock.tryLock(1L, TimeUnit.SECONDS);
        Thread.sleep(50 * 1000);
        if (!isLock) {
            log.error("获取锁失败 。。。。 1");
            return;
        }
        try {
            log.info("获取锁成功 。。。。 1");
            method2();
            log.info("开始执行业务 。。。。 1");
        } finally {
            log.warn("准备释放锁 。。。。 1");
            lock.unlock();
        }
    }

    @Test
    void method2() throws InterruptedException {
        // 尝试获取锁
        boolean isLock = lock.tryLock();
//        boolean isLock = lock.tryLock(100L, 1000L,TimeUnit.SECONDS);
        if (!isLock) {
            log.error("获取锁失败 。。。。 2");
            return;
        }
        try {
            log.info("获取锁成功 。。。。 2");
            log.info("开始执行业务 。。。。 2");
        } finally {
            log.warn("准备释放锁 。。。。 2");
            lock.unlock();
        }
    }




}
