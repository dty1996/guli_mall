package com.atguigu.gulimall.search.entity.params;

import lombok.Data;

import java.util.List;

/**
 * @author dty
 * @date 2022/9/6
 * @dec 描述
 */
@Data
public class SearchParam {
    /**
     * 过滤关键字
     */
    private String keyword ;

    /**
     * 第三级目录id
     */
    private Long catalog3Id;

    /**
     * 排序条件：只有一个
     * sort = saleCount_asc/desc
     * sort = skuPrice_asc/desc
     * sort = hotScore_asc/desc
     */
    private String sort;



    /**
     * 过滤条件
     */

    /**
     * 是否有库存
     */
    private Integer hasStock;
    /**
     * sku区间
     */
    private String skuPrice;
    /**
     * 品牌id,可以多个
     */
    private List<Long> brandIds;
    /**
     * 属性值，可以多个
     */
    private List<String> attrs;


    /**
     * 页码
     */
    private Integer pageNum;

}
