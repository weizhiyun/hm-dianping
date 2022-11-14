package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1、获取请求头中的token
        String token = request.getHeader("authorization");
        //2、基于token获取redis的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> UserMap = stringRedisTemplate.opsForHash().entries(key);
        //3、判断用户是否存在
        if (StrUtil.isBlank(token)) {
            return true;

        } else {
            //把查出来的Map转换成UserDTO对象
            UserDTO userDTO = BeanUtil.fillBeanWithMap(UserMap, new UserDTO(), false);
            UserHolder.saveUser(userDTO);
            //刷新Token的时间
            stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
            return true;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUser();
    }

    }
