package com.tong.service;

import com.tong.pojo.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IShopService extends IService<Shop> {

    Shop getByIdThroughCache(Long id);

    Long saveShop(Shop shop);

    void updateShop(Shop shop);

    List<Shop> pageShopByType(Integer typeId, Integer pageNum);

    List<Shop> pageShopByName(String name, Integer pageNum);
}
