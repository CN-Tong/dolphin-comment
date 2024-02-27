package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.exception.BusinessException;
import com.tong.mapper.VoucherOrderMapper;
import com.tong.pojo.entity.SeckillVoucher;
import com.tong.pojo.entity.VoucherOrder;
import com.tong.service.IVoucherOrderService;
import com.tong.utils.RedisIdWorker;
import com.tong.utils.SimpleRedisLock;
import com.tong.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long seckillVoucherById(Long voucherId) {
        // 1.查询优惠券
        SeckillVoucher seckillVoucher = Db.getById(voucherId, SeckillVoucher.class);
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
        // 创建优惠券订单
        Long userId = UserHolder.getUser().getId();
        // 创建分布式锁对象
        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, "order:" + userId);
        // 尝试获取锁
        boolean isLock = lock.tryLock(100);
        if (!isLock) {
            // 获取锁失败
            throw new BusinessException("请勿重复下单");
        }
        try {
            // 通过代理对象调用方法，以使事务生效，而非通过impl对象本身调用该方法
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Long createVoucherOrder(Long voucherId) {
        // 5.一人一单
        Long userId = UserHolder.getUser().getId();
        // 5.1根据voucherId和userId查询订单
        Long count = lambdaQuery()
                .eq(VoucherOrder::getVoucherId, voucherId)
                .eq(VoucherOrder::getUserId, userId)
                .count();
        // 5.2判断是否存在
        if (count > 0) {
            throw new BusinessException("每个用户只可购买一张");
        }
        // 6.扣减库存
        Db.lambdaUpdate(SeckillVoucher.class)
                .eq(SeckillVoucher::getVoucherId, voucherId)
                .gt(SeckillVoucher::getStock, 0)
                .setSql("stock = stock - 1")
                .update();
        // 7.创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        // 代金券id
        voucherOrder.setVoucherId(voucherId);
        // 用户id
        voucherOrder.setUserId(userId);
        // 订单id
        Long orderId = redisIdWorker.nextId("order:");
        voucherOrder.setId(orderId);
        save(voucherOrder);
        // 8.返回订单id
        return orderId;
    }
}
