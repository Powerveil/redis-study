package com.hmdp.controller;

import com.hmdp.service.TestService01;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController01
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/4/29 8:34
 * @Version 1.0
 */
@RestController
@RequestMapping("/TestController01")
public class TestController01 {
    @Autowired
    private TestService01 testService01;

    @GetMapping("/test11")
    public void test11(){
        testService01.test11();
    }
}
