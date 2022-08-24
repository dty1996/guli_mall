package com.atguigu.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.product.enums.AttrTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 商品属性
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;




    /**
     * 基本属性查询
     * @param params
     * @param categoryId
     * @return
     */
    @RequestMapping("/base/list/{categoryId}")
    public R baseAttrQuery(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long  categoryId){
        Integer attrType = AttrTypeEnum.BASE.getCode();
        PageUtils page = attrService.queryAttrPage(params, categoryId, attrType);

        return R.ok().put("page", page);
    }
    /**
     * 销售属性查询
     * @param params
     * @param categoryId
     * @return
     */
    @RequestMapping("/sale/list/{categoryId}")
    public R saleAttrQuery(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long  categoryId){
        Integer attrType = AttrTypeEnum.SALE.getCode();
        PageUtils page = attrService.queryAttrPage(params, categoryId, attrType);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    public R info(@PathVariable("attrId") Long attrId){
		AttrEntity attr = attrService.getById(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrEntity attr){
		attrService.save(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrEntity attr){
		attrService.updateById(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
