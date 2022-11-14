package com.hmdp;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class redistest extends ServiceImpl<ShopTypeMapper, ShopType> {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Test
    void testredis(){
        Map<String,String> ShopTypeList = new HashMap<>();
        List<ShopType> typeList = query().orderByAsc("sort").list();
        for (ShopType shopType : typeList){
            ShopTypeList.put(shopType.getName(), JSONUtil.toJsonStr(shopType));
        }
        stringRedisTemplate.opsForHash().putAll("shop_key",ShopTypeList);
    }
    @Test
    void test(){
        System.out.println("shide ");
    }
}
