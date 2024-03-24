package com.hmdp.utils;

/**
 * @ClassName ILock
 * @Description 分布式锁接口
 * @Author Powerveil
 * @Date 2024/3/24 22:16
 * @Version 1.0
 */
public interface ILock {
    /**
     * 尝试获取锁
     * @param timeoutSec 锁持有的超时时间，过期后自动释放
     * @return true代表索取锁成功;false代表获取锁失败
     */
    boolean tryLock(long timeoutSec);

    /**
     * 释放锁
     */
    void unlock();
}
