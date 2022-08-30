package com.atguigu.gulimall.ware.enums;

public enum PurchaseStatusEnum {

    NEW(0, "新建"),
    DISTRIBUTED(1, "已分配"),
    RECEIVED(2, "已领取"),
    COMPLETED(3, "已完成"),
    FAILED(4,"有异常");

    private Integer status;
    private String msg;


     PurchaseStatusEnum(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
