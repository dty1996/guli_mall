package com.atguigu.gulimall.order.thread;

import com.atguigu.common.to.LoginUserVo;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
public class UserThreadLocal {


    private static final ThreadLocal<LoginUserVo> USER_INFO = new ThreadLocal<>();


    public static LoginUserVo get() {
        return USER_INFO.get();
    }

    public static void set(LoginUserVo loginUserVo) {
        USER_INFO.set(loginUserVo);
    }

    public static void remove() {
        USER_INFO.remove();
    }
}
