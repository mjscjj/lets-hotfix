package com.github.lzy.hotfix.controller;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author liuzhengyang
 */
public class TestController {

    @RequestMapping("/test")
    public String hello() {
        return "hello";
    }
}
