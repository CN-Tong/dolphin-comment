package com.tong.controller;


import com.tong.result.Result;
import com.tong.service.ISeckillVoucherService;
import com.tong.service.IVoucherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
@Api(tags = "优惠券订单相关接口")
@Slf4j
public class VoucherOrderController {

    @Autowired
    private ISeckillVoucherService voucherOrderService;

    @PostMapping("seckill/{id}")
    @ApiOperation("秒杀优惠券下单")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        log.info("秒杀优惠券下单，voucherId：{}", voucherId);
        Long orderId = voucherOrderService.seckillVoucherById(voucherId);
        return Result.ok(orderId);
    }
}
