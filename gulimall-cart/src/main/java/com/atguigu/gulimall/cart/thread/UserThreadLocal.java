package com.atguigu.gulimall.cart.thread;

import com.atguigu.gulimall.cart.entity.to.UserInfoTo;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
public class UserThreadLocal {


    private static final ThreadLocal<UserInfoTo> USER_INFO = new ThreadLocal<>();


    public static UserInfoTo get() {
        return USER_INFO.get();
    }

    public static void set(UserInfoTo userInfoTo) {
        USER_INFO.set(userInfoTo);
    }

    public static void remove() {
        USER_INFO.remove();
    }
}
