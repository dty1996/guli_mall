package com.atguigu.gulimall.product.enums;

/**
 * @author Administrator
 */

public enum AttrTypeEnum {
    SALE(0, "销售属性"),
    BASE(1,"规格属性"),
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
