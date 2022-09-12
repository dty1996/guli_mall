package com.atguigu.common.exception;

/**
 * @author dty
 * @date 2022/8/16
 * @dec 异常枚举
 */
public enum BizExceptionEnum {
    UNKNOWN_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再发"),
    PRODUCT_UP_FAiL(10003, "商品上传出错");

    private Integer code;

    private String msg;

    BizExceptionEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
