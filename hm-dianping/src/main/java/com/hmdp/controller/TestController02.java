package com.hmdp.controller;

import com.hmdp.service.TestService01;
import com.hmdp.service.TestService02;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController02
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/4/29 8:35
 * @Version 1.0
 */
@RestController
@RequestMapping("/TestController02")
public class TestController02 {
    @Autowired
    private TestService02 testService02;

    @GetMapping("/test21")
    public void test21(){
        testService02.test21();
    }
}
