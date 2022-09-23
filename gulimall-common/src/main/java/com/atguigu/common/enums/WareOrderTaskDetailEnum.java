package com.atguigu.common.enums;

/**
 * @author dty
 * @date 2022/9/23 10:35
 * 库存工作单细节项状态枚举类
 */
public enum WareOrderTaskDetailEnum {
    /**
     * 未锁定
     */
    UNLOCK(0, "未解锁"),
    /**
     * 已锁定
     */
    LOCK(1,"已解锁");

    private Integer code;
    private String msg;

    WareOrderTaskDetailEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public Integer getCode() {
        return code;
    }
}
