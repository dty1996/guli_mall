package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.search.entity.params.SearchParam;
import com.atguigu.gulimall.search.entity.vo.SearchResponseVo;
import com.atguigu.gulimall.search.service.MallSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService mallSearchService;

    @GetMapping("list.html")
    public String listPage(SearchParam searchParam, Model model) {
        SearchResponseVo result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
