package com.tong.controller;


import com.tong.result.Result;
import com.tong.pojo.entity.Shop;
import com.tong.service.IShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/shop")
@Api(tags = "商铺相关接口")
@Slf4j
public class ShopController {

    @Resource
    public IShopService shopService;

    @GetMapping("/{id}")
    @ApiOperation("根据id查询商铺信息")
    public Result queryShopById(@PathVariable("id") Long id) {
        log.info("根据id查询商铺信息，id：{}", id);
        return Result.ok(shopService.getById(id));
    }

    @PostMapping
    @ApiOperation("新增商铺")
    public Result saveShop(@RequestBody Shop shop) {
        log.info("新增商铺，shop：{}", shop);
        Long shopId = shopService.saveShop(shop);
        return Result.ok(shopId);
    }

    @PutMapping
    @ApiOperation("更新商铺")
    public Result updateShop(@RequestBody Shop shop) {
        log.info("更新商铺，shop：{}", shop);
        shopService.updateById(shop);
        return Result.ok();
    }

    @GetMapping("/of/type")
    @ApiOperation("根据商铺类型分页查询商铺信息")
    public Result queryShopByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer pageNum
    ) {
        log.info("根据商铺类型分页查询商铺信息，typeId：{}，pageNum：{}", typeId, pageNum);
        List<Shop> records = shopService.pageShopByType(typeId, pageNum);
        return Result.ok(records);
    }

    @GetMapping("/of/name")
    @ApiOperation("根据商铺名称关键字分页查询商铺信息")
    public Result queryShopByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer pageNum
    ) {
        log.info("根据商铺名称关键字分页查询商铺信息，name：{}，pageNum：{}", name, pageNum);
        List<Shop> records = shopService.pageShopByName(name, pageNum);
        return Result.ok(records);
    }
}
