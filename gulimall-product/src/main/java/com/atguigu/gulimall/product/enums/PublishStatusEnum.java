package com.atguigu.gulimall.product.enums;

/**
 * @author dty
 * @date 2022/9/1
 * @dec 描述
 */
public enum PublishStatusEnum {

    DOWN(0,"下架"),
    PUBLISHED(1, "上架");

    private Integer code;

    private String msg;

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    PublishStatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
