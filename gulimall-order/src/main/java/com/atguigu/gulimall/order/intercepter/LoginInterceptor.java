package com.atguigu.gulimall.order.intercepter;

import com.atguigu.common.to.LoginUserVo;
import com.atguigu.common.utils.Constant;
import com.atguigu.gulimall.order.thread.UserThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 登陆拦截器
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

        HttpSession session = request.getSession();
        LoginUserVo loginUserVo = (LoginUserVo) session.getAttribute(Constant.LOGIN_USER);

        //判断用户是否登录
        if (null != loginUserVo) {
            UserThreadLocal.set(loginUserVo);
            return true;
        }
        session.setAttribute("msg", "请先登录");
        response.sendRedirect("http://auth.gulimall.com/");
        return false;
    }

}
