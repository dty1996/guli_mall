package com.atguigu.gulimall.product.web;


import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.entity.vo.Catalog2Vo;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author dty
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index.html"})
    public String  index(Model model){

        //1、查出所有的一级分类
        List<CategoryEntity> categoryEntities  = categoryService.getLevel1Categorys();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }

    @GetMapping("index/json/catalog")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson(){

//        return categoryService.getCatalogJson();
        return   categoryService.getCatalogJsonFromDbWithRedisLock();
    }

}
