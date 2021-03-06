package com.winterice.vote.interceptor;


import com.winterice.vote.annotation.Auth;
import com.winterice.vote.exception.UnloginException;
import com.winterice.vote.utils.JwtTokenUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;


public class LoginInterceptor implements HandlerInterceptor {
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //放行预检请求
        String method = request.getMethod();
        if(HttpMethod.OPTIONS.matches(method)){
            return true;
        }
        //检查注解
        HandlerMethod hand = (HandlerMethod) handler;
        Auth auth = AnnotationUtils.findAnnotation(((HandlerMethod) handler).getMethod(), Auth.class);
        //无需鉴权，直接放行
        if (auth == null) {
            return true;
        }
        String target = auth.value();
        //鉴权逻辑
        if(checkRole(request, hand, target, response)){
            return true;
        }else{
            throw new UnloginException("您未登录或权限不足");
        }
    }

    private boolean checkRole(HttpServletRequest request, HandlerMethod hand, String target, HttpServletResponse response) throws Exception{
        String role = request.getHeader("role");
        String token = request.getHeader("Authorization");
        if(token == null){
            return false;
        }else{
            token = token.replaceAll("Bearer ","");
        }
        List<String> roles = JwtTokenUtils.getUserRolesByToken(token);
        if(roles.contains(target)){
            userId.set(JwtTokenUtils.getUsernameByToken(token));
            response.addHeader("userId", JwtTokenUtils.getUsernameByToken(token));
            return true;
        }else {
            return false;
        }
    }

    public static String getUserId(){
        return userId.get();
    }
}


