package com.atguigu.gulimall.product.enums;

/**
 * @author Administrator
 */

public enum AttrTypeEnum {
    BASE(0,"规格属性"),
    SALE(1, "销售属性"),
    BOTH(2, "规格销售属性");


    private Integer code;
    private String des;

    public Integer getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    AttrTypeEnum(Integer code, String des) {
        this.code = code;
        this.des = des;
    }
}
