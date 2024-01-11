package com.tong.service.impl;

import com.tong.pojo.entity.ShopType;
import com.tong.mapper.ShopTypeMapper;
import com.tong.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Override
    public List<ShopType> listType() {
        List<ShopType> shopTypeList = lambdaQuery()
                .orderByAsc(ShopType::getSort)
                .list();
        return shopTypeList;
    }
}
