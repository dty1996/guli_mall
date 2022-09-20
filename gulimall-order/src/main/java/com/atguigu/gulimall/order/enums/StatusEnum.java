package com.atguigu.gulimall.order.enums;


/**
 *
 * @author Administrator
 * 订单状态枚举
 */

public enum StatusEnum {
    TO_SEND(1, "待发货"),
    SEND(2, "已发货"),
    COMPLETED(3, "已完成"),
    CLOSED(4, "已关闭"),
    INVALID(5,"无效订单");

    private Integer code;
    private String msg;

    StatusEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}