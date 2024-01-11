package com.tong.service;

import com.tong.pojo.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IShopTypeService extends IService<ShopType> {

    List<ShopType> listType();
}
