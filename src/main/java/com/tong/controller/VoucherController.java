package com.tong.controller;


import com.tong.result.Result;
import com.tong.pojo.entity.Voucher;
import com.tong.service.IVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/voucher")
@Api(tags = "优惠券相关接口")
@Slf4j
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    @PostMapping
    @ApiOperation("新增普通券")
    public Result addVoucher(@RequestBody Voucher voucher) {
        log.info("新增普通券，voucher：{}", voucher);
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    @PostMapping("seckill")
    @ApiOperation("新增秒杀券")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        log.info("新增秒杀券，voucher：{}", voucher);
        voucherService.saveSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    @ApiOperation("查询指定id的店铺的优惠券列表")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        log.info("查询指定id的店铺的优惠券列表，shopId：{}", shopId);
        List<Voucher> voucherList = voucherService.listVoucherByShopId(shopId);
        return Result.ok(voucherList);
    }
}
