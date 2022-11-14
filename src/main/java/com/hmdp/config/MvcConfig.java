package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    //因为这个类是交给spring容器进行管理的，可以直接使用
    //还有一个方法就是直接用c@onponent注解也行
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                        .addPathPatterns("/**");
        registry
        .addInterceptor(new LoginInterceptor()).
                excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "blog/hot",
                        "/shop/**",
                        "/shop-type/**"
                );
    }
}
