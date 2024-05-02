package com.hmdp.service.impl;

import com.hmdp.service.IBlogService;
import com.hmdp.service.TestService01;
import com.hmdp.service.TestService02;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName TestServiceImpl
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/4/29 8:24
 * @Version 1.0
 */
@Component
public class TestService01Impl implements TestService01 {
    @Autowired
    private TestService02 testService02;
    @Override
    public void test11() {
        testService02.print21();
    }

    @Override
    public void print12() {
        System.out.println("TestService01Impl");
    }
}
