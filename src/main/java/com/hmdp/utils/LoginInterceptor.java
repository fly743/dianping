package com.hmdp.utils;

import com.hmdp.constant.SessionConstants;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.removeUser();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取当前线程的session
        HttpSession session = request.getSession();
        //获取 session的用户
        Object user = session.getAttribute(SessionConstants.LOGIN_USER);
        //判断用户是否存在
        if (user == null){
            response.setStatus(401);
            return false;
        }
        //存在，保存用户
        UserHolder.saveUser((UserDTO) user);
        return true;
    }

}
