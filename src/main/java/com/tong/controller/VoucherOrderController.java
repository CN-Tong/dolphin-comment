package com.tong.controller;


import com.tong.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
@Api(tags = "优惠券订单相关接口")
@Slf4j
public class VoucherOrderController {

    @PostMapping("seckill/{id}")
    @ApiOperation("根据id查询秒杀订单")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        log.info("根据id查询秒杀券，voucherId：{}", voucherId);
        return Result.fail("功能未完成");
    }
}
