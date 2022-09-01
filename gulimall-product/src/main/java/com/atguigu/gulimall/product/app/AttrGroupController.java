package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.params.AttrAttrgroupRelationParam;
import com.atguigu.gulimall.product.entity.vo.AttrgroupWithAttrsVo;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;


/**
 * 属性分组
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private AttrService attrService;

    /**
     * 列表
     */
    @RequestMapping("/list/{categoryId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("categoryId") Long categoryId){
        PageUtils page = attrGroupService.queryPage(params, categoryId);

        return R.ok().put("page", page);
    }



    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.selectPathByCategotyId(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    @RequestMapping("{attrgroupId}/attr/relation")
    public R attrAndAttrgroupRelation(@RequestParam Map<String, Object> params ,
                                      @PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> attrList = attrGroupService.selectAttrByAttrgroupId(params, attrgroupId);
        return R.ok().put("data", attrList);
    }

    @RequestMapping("{catelogId}/withattr")
    public R queryAttrgroupWithAttrByCatelogId(@PathVariable("catelogId") Long catelogId){
        List<AttrgroupWithAttrsVo> attrgroupWithAttrsVos = attrGroupService.queryAttrgroupWithAttrByCatelogId(catelogId);
        return R.ok().put("data", attrgroupWithAttrsVos);
    }
    /**
     * 列表
     */
    @RequestMapping("{attrgroupId}/noattr/relation")
    public R queryAttrNoAttrgroup(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId")Long attrgroupId){
        PageUtils page = attrService.queryAttrNoAttrgroup(params, attrgroupId);

        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation/delete")
    public R delete(@RequestBody List<AttrAttrgroupRelationParam> params){
        attrAttrgroupRelationService.removeParams(params);

        return R.ok();
    }

    @PostMapping("/attr/relation")
    public R save(@RequestBody List<AttrAttrgroupRelationEntity> list) {

        attrAttrgroupRelationService.saveBatch(list);

        return R.ok();
    }
}
