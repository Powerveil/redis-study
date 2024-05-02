package com.hmdp.service.impl;

import com.hmdp.service.TestService01;
import com.hmdp.service.TestService02;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName TestService02Impl
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/4/29 8:29
 * @Version 1.0
 */
@Component
public class TestService02Impl implements TestService02 {
    @Autowired
    private TestService01 testService01;

    @Override
    public void test21() {
        testService01.print12();
    }

    @Override
    public void print21() {
        System.out.println("TestService02Impl");
    }
}
