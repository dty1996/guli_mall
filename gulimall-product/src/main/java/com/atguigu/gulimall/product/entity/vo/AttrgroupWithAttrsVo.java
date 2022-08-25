package com.atguigu.gulimall.product.entity.vo;

import com.atguigu.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author dty
 * @date 2022/8/25
 * @dec 描述
 */
@Data
public class AttrgroupWithAttrsVo {

    private Long attrGroupId;
    private String attrGroupName;
    private Integer sort;
    private String  icon;
    private Long catelogId;
    private List<AttrEntity> attrs;


}
