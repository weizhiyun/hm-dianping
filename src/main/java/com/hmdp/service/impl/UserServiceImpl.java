package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
//为什么要extends ServiceImpl这个类：可以直接使用myb-plus来实现单表查询。
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    //使用springdata提供的对redis的操作
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendcood(String phone, HttpSession session){
        //校验手机号,手机号是否符合规范，不符合就返回错误信息，符合就发送验证码
        //写了一个工具类来进行校验就行
        if (RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //生成随机数，生成校验码
        String code = RandomUtil.randomNumbers(6);
        //保存校验码在redis中,使用string类型来实现，前面加上一个login前缀进行一个区分,设置时间是两分钟
        stringRedisTemplate.opsForValue().
                set(LOGIN_CODE_KEY+phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //发送验证码,一般是调用第三方平台来实现(这个可以之后进行补充实现)
        log.debug("发送短信验证码成功，验证码：{}",code);
        return Result.ok();
    }
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session){
        //校验手机号，这个最好要校验一下和之前session存的手机是不是一致的
        //所以在上面要改变session的方法，可以把phone
       String phone = loginForm.getPhone();
        //检验验证码（从redis里面进行获取）,为什么这个会获取出来的是Null
        String cachcode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY+phone);
        String code = loginForm.getCode();
        System.out.println(cachcode);
        System.out.println(code);
        System.out.println(cachcode==code);
        if(cachcode == null || !cachcode.toString().equals(code)){
            return Result.fail("验证码错误");
        }
        User user = query().eq("phone",phone).one();
        if(user == null){
            //创建新用户，创建之后注意，也要把新的这个User放到session里面
            user = createUserWithPhone(phone);
        }
        //使用工具类BeanUtil将user类型转换成UserDTO类型
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //把用户信息保存在redis中
        //1、随机生成Token,使用工具类随机生成字符串
        String token = UUID.randomUUID().toString(true);
        //2、将User对象转换成Hash进行存储
        //2.1将User拷贝到UserDTO1进行存储粒度的划分
        UserDTO userDTO1  = BeanUtil.copyProperties(user, UserDTO.class);
        //2.2将UserDTO1转换成Map,并且使用这个工具类把所有字段都转换成string类型
        Map<String,Object> usermap = BeanUtil.beanToMap(userDTO1,new HashMap<>(),
                CopyOptions.
                        create().
                        setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
                        //里面这三个表示两个参数和返回值，这个是java高级篇的函数接口知识点
        //3、存储(现在这里的编写是不符合事务的原子性的，可以参考后面的lua版本)
        stringRedisTemplate.opsForHash().
                putAll(LOGIN_USER_KEY+token,usermap);
        //设置过期时间
        stringRedisTemplate.expire(LOGIN_USER_KEY+token,LOGIN_USER_TTL,TimeUnit.MINUTES);
        //把Token返回给前端
        //
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX +RandomUtil.randomString(10));
        return user;
    }


}
