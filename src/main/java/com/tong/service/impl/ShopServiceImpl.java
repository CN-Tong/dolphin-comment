package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.pojo.entity.Shop;
import com.tong.mapper.ShopMapper;
import com.tong.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.utils.SystemConstants;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

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
