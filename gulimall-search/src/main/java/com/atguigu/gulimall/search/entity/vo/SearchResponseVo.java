package com.atguigu.gulimall.search.entity.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

/**
 * @author dty
 * @date 2022/9/6
 * @dec 描述
 */
@Data
public class SearchResponseVo {
    /**
     * 商品信息
     */
    private List<SkuEsModel> products;


    /**
     *  分页信息
     */
    private Integer pageNum;
    private Long total;
    private Integer totalPage;

    /**
     * 查询结果涉及到的品牌
     */
    private List<BrandVo> brands;

    /**
     * 查询结果涉及到的属性值
     */
    private List<AttrVo> attrs;

    /**
     * 涉及到的分类
     */
    private List<CatalogVo> catalogs;


    //=========以上是返回给页面的所有信息========

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo {
        private Long attId;
        private String attrName;
        private List<String> attrValue;
    }


    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }


}
