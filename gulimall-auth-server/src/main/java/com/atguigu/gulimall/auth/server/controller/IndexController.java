package com.atguigu.gulimall.auth.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author dty
 * @date 2022/9/12
 * @dec 描述
 */
@Controller
public class IndexController {

    @GetMapping({"/","login.html"})
    public String login() {

        return "login";
    }

    @GetMapping("reg.html")
    public String reg() {

        return "reg";
    }
}
