package com.atguigu.gulimall.order.thread;

import com.atguigu.gulimall.order.entity.vo.SubmitOrderVo;

/**
 * @author Administrator
 */
public class OrderThreadLocal {
    private static final ThreadLocal<SubmitOrderVo> ORDER = new ThreadLocal<>();


    public static SubmitOrderVo get() {
        return ORDER.get();
    }

    public static void set(SubmitOrderVo submitOrderVo) {
        ORDER.set(submitOrderVo);
    }

    public static void remove() {
        ORDER.remove();
    }
}
