package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result querygetById(Long id) {
        //1、从redis中查询商户缓存
        String shop = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        /*
        从缓存中查询出来的时候会出现三种情况：
        为null->查询数据库
        不为null但是为空字符串->数据库中都不存在这个东西
        为非空字符串->返回响应的数据
        */
        //2、判断是否存在,这个判断的是只有是“abc”这种情况才会走这条路（查看源码注释）
        if (StrUtil.isNotBlank(shop)) {
            //3、存在直接返回(要从JSON转换成shop对象的类型)
            Shop shop1 = JSONUtil.toBean(shop, Shop.class);
            return Result.ok(shop);

        }
        if(shop != null){
            //shop不为null，且不走第一条，说明查出来的是一个空字符串“”
            return Result.fail("店铺不存在");
        }
        //4、不存在，就根据商户id在数据库中查询
        else {
            //这个写法真的新奇啊！！
            Shop shop1 = getById(id);
            if (shop1 == null) {
                //为了解决缓存穿透的问题，返回null的时候我们就要把null值也写入缓存
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id,
                        "",
                        CACHE_NULL_TTL,
                        TimeUnit.MINUTES);
                return Result.fail("店铺不存在！");
            } else {
                //使用的是字符串的形式进行redis存储,并且进行超时设置
                stringRedisTemplate.opsForValue().
                        set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(shop1),
                                CACHE_SHOP_TTL,
                                TimeUnit.MINUTES);

                return Result.ok(shop1);
            }
        }
        //5、不存在，返回错误
        //6、存在，返回数据，并且写在redis中

    }

    @Override
    //对数据库进行更新，并且写入缓存
    @Transactional(rollbackFor = Exception.class)//这个注解是表示这个要有事务性，这个是spring的事务管理和数据库的事务是不一致的（去补课，spring处理事务）
    //保证如果删除出现异常，前面的操作就会回滚
    public Result updateshop(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        //更新数据库
        updateById(shop);
        //删除缓存,不用写了，等查询之后自然会写进去
        stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        return Result.ok();

    }



}
