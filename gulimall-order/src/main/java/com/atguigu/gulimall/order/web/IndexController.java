package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author dty
 * @date 2022/9/18
 * @dec 描述
 */
@Controller
public class IndexController {

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page) {
        return page;
    }
}
