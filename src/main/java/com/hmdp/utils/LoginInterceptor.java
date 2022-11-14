package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginInterceptor implements HandlerInterceptor {
    //一个知识点,LoginInterceptor是我们自己进行创建的类，是不可以利用反射注入的，只要交给spring容器管理的才可以
    //所以只能使用的有参构造进行


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //上一个拦截器RefreshTokenInterceptor，已经把所有都拦截了，这里只需要判断是不是已经存在User了就行了
        if(UserHolder.getUser() == null){
            response.setStatus(401);
            return false;
        }
        else return true;
    }


}
