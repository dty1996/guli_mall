package com.atguigu.gulimall.product.enums;

/**
 * 是否检索枚举类
 * @author Administrator
 */

public enum SearchTypeEnum {

    CAN(1, "需要检索"),
    CANNOT(0,"不需要检索");



    private Integer code;
    private String msg;

    SearchTypeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
