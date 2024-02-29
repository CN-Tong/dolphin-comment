package com.tong.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.entity.Shop;
import com.tong.mapper.ShopMapper;
import com.tong.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.SystemConstants;
import com.tong.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Shop getByIdThroughCache(Long id) {
        // 解决缓存穿透
        Shop shop = getByIdWithPassThrough(id);

        // 基于互斥锁解决缓存击穿
        // Shop shop = getByIdWithMutex(id);
        // if(shop == null){
        //     throw new BusinessException("店铺不存在！");
        // }

        // 基于逻辑过期解决缓存击穿
        // Shop shop = getByIdWithLogicalExpire(id);
        return shop;
    }

    /**
     * 解决缓存穿透
     */
    public Shop getByIdWithPassThrough(Long id) {
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从Redis查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            // 如果是空值，返回错误信息
            throw new BusinessException("商铺不存在！");
        }
        // 3.不存在，根据id查询数据库
        Shop shop = getById(id);
        // 4.判断是否存在
        if (shop == null) {
            // 不存在，将空值写入Redis，返回错误信息
            stringRedisTemplate.opsForValue()
                    .set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            throw new BusinessException("商铺不存在！");
        }
        // 5.存在，写入Redis
        stringRedisTemplate.opsForValue()
                .set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        // 6.返回商铺信息
        return shop;
    }

    /**
     * 基于互斥锁解决缓存击穿
     */
    public Shop getByIdWithMutex(Long id) {
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从Redis查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在，直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        // 判断命中的是否是空值
        if (shopJson != null) {
            // 如果是空值，返回错误信息
            throw new BusinessException("商铺不存在！");
        }
        // 3.不存在，实现缓存重建
        // 3.1获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            // 3.2判断是否获取成功
            if (!isLock) {
                // 获取失败，休眠并重试
                Thread.sleep(50);
                getByIdWithMutex(id);
            }
            // 3.3获取成功，再次判断Redis缓存是否存在，做二次校验
            shopJson = stringRedisTemplate.opsForValue().get(shopKey);
            if (StrUtil.isNotBlank(shopJson)) {
                // 存在，直接返回
                return JSONUtil.toBean(shopJson, Shop.class);
            }
            // 3.4Redis缓存不存在，根据id查询数据库
            shop = getById(id);
            // 模拟重建延迟，检验锁可不可靠
            Thread.sleep(200);
            // 4.判断是否存在
            if (shop == null) {
                // 不存在，将空值写入Redis，返回错误信息
                stringRedisTemplate.opsForValue()
                        .set(shopKey, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                throw new BusinessException("商铺不存在！");
            }
            // 5.存在，写入Redis
            stringRedisTemplate.opsForValue()
                    .set(shopKey, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 6.释放互斥锁
            unlock(lockKey);
        }
        // 7.返回商铺信息
        return shop;
    }

    /**
     * 尝试获取互斥锁
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放互斥锁
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 线程池对象
     */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * 基于互斥锁解决缓存击穿
     */
    public Shop getByIdWithLogicalExpire(Long id) {
        String shopKey = RedisConstants.CACHE_SHOP_KEY + id;
        // 1.从Redis查询商户缓存
        String shopJson = stringRedisTemplate.opsForValue().get(shopKey);
        // 2.判断是否存在
        if (StrUtil.isBlank(shopJson)) {
            // 不存在，直接返回空
            return null;
        }
        // 3.存在，判断是否过期
        // JSON反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        JSONObject data = (JSONObject) redisData.getData();
        Shop shop = JSONUtil.toBean(data, Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 判断是否过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回商铺信息
            return shop;
        }
        // 4.已过期，需要缓存重建
        // 4.1获取互斥锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        // 4.2判断是否获取成功
        if (isLock) {
            // 5.获取成功，开启独立线程重建缓存（使用线程池）
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // 6.释放互斥锁
                    unlock(lockKey);
                }
            });
        }
        // 7.返回商铺信息
        return shop;
    }

    /**
     * 模拟缓存带有逻辑过期时间的商铺信息（预热）
     */
    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        // 1.查询商铺数据
        Shop shop = getById(id);
        // 2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        // 3.写入Redis
        stringRedisTemplate.opsForValue()
                .set(RedisConstants.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    public Long saveShop(Shop shop) {
        save(shop);
        return shop.getId();
    }

    @Override
    @Transactional
    public void updateShop(Shop shop) {
        Long shopId = shop.getId();
        if (shopId == null) {
            throw new BusinessException("店铺id不能为空！");
        }
        // 1.更新数据库
        updateById(shop);
        // 2.删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shopId);
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
