package com.atguigu.gulimall.order.enums;

/**
 * @author Administrator
 */

public enum ConfirmStatusEnum {

    NOT_CHECK(0, "未确认"),
    CHECKED(1, "已确认");

    private Integer code;
    private String msg;

    ConfirmStatusEnum(Integer code, String msg) {
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
