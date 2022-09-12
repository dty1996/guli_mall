package com.atguigu.gulimall.product.entity.vo;

import lombok.Data;
import java.util.List;

@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuAttrVo> attrs;
}
