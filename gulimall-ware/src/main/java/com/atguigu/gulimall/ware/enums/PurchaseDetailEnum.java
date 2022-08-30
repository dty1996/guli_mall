package com.atguigu.gulimall.ware.enums;

public enum PurchaseDetailEnum {
    NEW(0, "新建"),
    DISTRIBUTED(1, "已分配"),
    PURCHASEING(2, "采购中"),
    COMPLETED(3, "已完成"),
    FAILED(4,"失败");

    private Integer status;
    private String msg;


    PurchaseDetailEnum(Integer status, String msg) {
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
