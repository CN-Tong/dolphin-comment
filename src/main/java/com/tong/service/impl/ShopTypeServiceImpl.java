package com.tong.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.entity.Shop;
import com.tong.pojo.entity.ShopType;
import com.tong.mapper.ShopTypeMapper;
import com.tong.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> listType() {
        String shopTypeKey = RedisConstants.CACHE_SHOP_TYPE_KEY;
        // 1.从Redis中查询
        String shopTypeListJson = stringRedisTemplate.opsForValue().get(shopTypeKey);
        // 2.判断是否为空
        if (StrUtil.isNotBlank(shopTypeListJson)) {
            // 不为空，直接返回
            return JSONUtil.toList(shopTypeListJson, ShopType.class);
        }
        // 3.为空，查询数据库
        List<ShopType> shopTypeList = lambdaQuery()
                .orderByAsc(ShopType::getSort)
                .list();
        // 4.判断是否为空
        if (CollUtil.isEmpty(shopTypeList)) {
            // 为空，抛出异常
            throw new BusinessException("暂无任何商铺类型！");
        }
        // 5.不为空，写入Redis
        stringRedisTemplate.opsForValue().set(shopTypeKey, JSONUtil.toJsonStr(shopTypeList));
        // 6.返回商铺类型的集合
        return shopTypeList;
    }
}
