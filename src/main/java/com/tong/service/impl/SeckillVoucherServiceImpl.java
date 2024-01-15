package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.exception.BusinessException;
import com.tong.pojo.entity.SeckillVoucher;
import com.tong.mapper.SeckillVoucherMapper;
import com.tong.pojo.entity.VoucherOrder;
import com.tong.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.utils.RedisIdWorker;
import com.tong.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Override
    @Transactional
    public Long seckillVoucherById(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher seckillVoucher = getById(voucherId);
        // 2.判断秒杀是否开始
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("秒杀尚未开始");
        }
        // 3.判断秒杀是否结束
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("秒杀已经结束");
        }
        // 4.判断库存是否充足
        if (seckillVoucher.getStock() < 1) {
            throw new BusinessException("库存不足");
        }
        // 5.扣减库存
        lambdaUpdate()
                .eq(SeckillVoucher::getVoucherId, voucherId)
                .gt(SeckillVoucher::getStock, 0)
                .setSql("stock = stock - 1")
                .update();
        // 6.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 代金券id
        voucherOrder.setVoucherId(voucherId);
        // 用户id
        voucherOrder.setUserId(UserHolder.getUser().getId());
        // 订单id
        Long orderId = redisIdWorker.nextId("order:");
        voucherOrder.setId(orderId);
        Db.save(voucherOrder);
        // 7.返回订单id
        return orderId;
    }
}
