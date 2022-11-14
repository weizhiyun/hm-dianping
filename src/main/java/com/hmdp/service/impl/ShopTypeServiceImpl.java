package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void GetShopTypeList() {
        //1、先去缓存中查询,主要是弄清怎么存，可以使用字符串，也可以使用List,还没有说
        //查出来的是字符串，要怎么拆分成一个一个的对象
        List<ShopType> l1 = new ArrayList<>();
        Map<Object,Object> s= stringRedisTemplate.opsForHash().entries("shop_key");
        Collection<Object> l=  s.values();
        for (Object o:l){
            String st = (String) o;
            l1.add(BeanUtil.toBean(o,ShopType.class));
        }
        System.out.println(s);
        //2、查询不到就从数据库中查询
        Map<String,String> ShopTypeList = new HashMap<>();
        List<ShopType> typeList = query().orderByAsc("sort").list();
        for (ShopType shopType : typeList){
                ShopTypeList.put(shopType.getName(), JSONUtil.toJsonStr(shopType));
        }
        stringRedisTemplate.opsForHash().putAll("shop_key",ShopTypeList);

    }
}
