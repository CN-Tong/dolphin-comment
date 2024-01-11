package com.tong.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.entity.Shop;
import com.tong.mapper.ShopMapper;
import com.tong.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop getByIdThroughCache(Long id) {
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从Redis查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
        // 2.判断是否存在
        if(StrUtil.isNotBlank(shopJson)){
            // 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 3.不存在，根据id查询数据库
        Shop shop = getById(id);
        // 4.判断是否存在
        if(shop == null) {
            // 不存在，抛出异常
            throw new BusinessException("商铺不存在！");
        }
        // 5.存在，写入Redis
        stringRedisTemplate.opsForValue().set(shopKey, JSONUtil.toJsonStr(shop));
        // 6.返回商铺信息
        return shop;
    }

    @Override
    public Long saveShop(Shop shop) {
        save(shop);
        return shop.getId();
    }

    @Override
    public List<Shop> pageShopByType(Integer typeId, Integer pageNum) {
        Page<Shop> page = Page.of(pageNum, SystemConstants.DEFAULT_PAGE_SIZE);
        Page<Shop> p = lambdaQuery()
                .eq(Shop::getTypeId, typeId)
                .page(page);
        return p.getRecords();
    }

    @Override
    public List<Shop> pageShopByName(String name, Integer pageNum) {
        Page<Shop> page = Page.of(pageNum, SystemConstants.DEFAULT_PAGE_SIZE);
        Page<Shop> p = lambdaQuery()
                .like(Shop::getName, name)
                .page(page);
        return p.getRecords();
    }
}
