package com.atguigu.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atguigu.gulimall.product.entity.ProductAttrValueEntity;
import com.atguigu.gulimall.product.entity.vo.AttrVo;
import com.atguigu.gulimall.product.enums.AttrTypeEnum;
import com.atguigu.gulimall.product.service.ProductAttrValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    @Autowired
    private ProductAttrValueService productAttrValueService;


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
     * 根据spuid查询spu信息
     * @param spuId
     * @return
     */
    @RequestMapping("base/listforspu/{spuId}")
    public R listForSpu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity>  list = productAttrValueService.queryBySpu(spuId);
        return R.ok().put("data", list);
    }

    @PostMapping("update/{spuId}")
    public R updateBySpuId(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> productAttrValueEntities ) {
        productAttrValueService.updateBySpuId(spuId, productAttrValueEntities);
        return R.ok();
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
        AttrVo attrVo = attrService.selectAttrVoInfo(attrId);

        return R.ok().put("attr", attrVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrEntity attr){
		attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrEntity attr){
		attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeAttr(Arrays.asList(attrIds));

        return R.ok();
    }

}
