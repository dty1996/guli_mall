package com.atguigu.gulimall.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Administrator
 */
@Controller
public class SearchController {


    @GetMapping("list.html")
    public String listPage(Model model) {

        return "list";
    }
}
