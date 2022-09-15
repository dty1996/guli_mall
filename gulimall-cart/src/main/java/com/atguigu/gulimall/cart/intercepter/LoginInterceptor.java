package com.atguigu.gulimall.cart.intercepter;

import com.atguigu.common.to.LoginUserVo;
import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.cart.entity.to.UserInfoTo;
import com.atguigu.gulimall.cart.thread.UserThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {


    /**
     * 方法执行之前 ： 判断用户是否登录
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        LoginUserVo loginUserVo = (LoginUserVo) session.getAttribute(Constant.LOGIN_USER);

        //判断用户是否登录
        if (null != loginUserVo) {
            userInfoTo.setUserId(loginUserVo.getId());
        }

        //判断是否有user_key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (Constant.USER_KEY.equals(name)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTemUser(true);
                }
            }
        }
        if (!userInfoTo.isTemUser()) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }
        UserThreadLocal.set(userInfoTo);
        return true;
    }


    /**
     * 方法执行之后
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = UserThreadLocal.get();
        if (!userInfoTo.isTemUser()) {
            Cookie cookie = new Cookie(Constant.USER_KEY, userInfoTo.getUserKey());
            cookie.setDomain("gulimall0.com");
            cookie.setMaxAge(Constant.MAX_AGE);
            response.addCookie(cookie);
        }

    }
}
