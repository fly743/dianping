package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

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
    public Result queryTypeList() {

        String key = CACHE_SHOP_TYPE_KEY;
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(key,0,-1);
        ShopType shopType = null;
        //判断缓存是否命中
        if(CollectionUtil.isNotEmpty(shopTypeJson)){
            List<ShopType> shopTypes = shopTypeJson.stream()
                    .map(json -> JSONUtil.toBean(json, ShopType.class))
                    .collect(Collectors.toList());
            shopTypes.sort(((o1, o2) -> o1.getSort() - o2.getSort()));
            return Result.ok(shopTypes);
        }
        //缓存未命中，查询数据库
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        //不存在，返回错误
        if(CollectionUtil.isEmpty(shopTypes)){
            return Result.fail("店铺类型不存在");
        }
        //存在，写入redis
        List< String> shopTypesJson = shopTypes.stream()
                .map(Type->JSONUtil.toJsonStr(Type))
                .collect(Collectors.toList());
        stringRedisTemplate.opsForList().rightPushAll(key,shopTypesJson);
        return Result.ok(shopTypes);
    }


}
