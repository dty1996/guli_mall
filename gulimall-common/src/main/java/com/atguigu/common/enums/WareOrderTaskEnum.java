package com.atguigu.common.enums;

/**
 * @author dty
 * @date 2022/9/23 10:35
 * 库存工作单状态枚举类
 */
public enum WareOrderTaskEnum {
    /**
     * 未锁定
     */
    UNLOCK(0, "未锁定"),
    /**
     * 已锁定
     */
    LOCK(1,"已锁定");

    private Integer code;
    private String msg;

    WareOrderTaskEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
