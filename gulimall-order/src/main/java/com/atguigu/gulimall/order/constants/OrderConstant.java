package com.atguigu.gulimall.order.constants;

/**
 * @author Administrator
 */
public class OrderConstant {
    /**
     * 订单token前缀
     */
    public static final String ORDER_TOKEN_PREFIX = "order:token:";

    /**
     * token过期时间30分钟
     */
    public static final long ORDER_TOKEN_EXPIRE = 30 * 60;

    /**
     * 比较删除成功结果：1L
     */
    public static final Long SUCCESS_CAD = 1L;

    /**
     * 自动确认时间 7天
     */
    public static final Integer DEFAULT_CONFIRM_DAY = 7;

    /**
     * 误差允许价格 0.01圆
     */
    public static final double DEVIATION_PRICE = 0.01;
}
