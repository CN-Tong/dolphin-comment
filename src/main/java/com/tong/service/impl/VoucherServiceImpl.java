package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.pojo.entity.Voucher;
import com.tong.mapper.VoucherMapper;
import com.tong.pojo.entity.SeckillVoucher;
import com.tong.service.IVoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Override
    @Transactional
    public void saveSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = BeanUtil.copyProperties(voucher, SeckillVoucher.class);
        seckillVoucher.setVoucherId(voucher.getId());
        Db.save(seckillVoucher);
    }

    @Override
    public List<Voucher> listVoucherByShopId(Long shopId) {
        List<Voucher> voucherList = lambdaQuery()
                .eq(Voucher::getShopId, shopId)
                .list();
        voucherList.forEach(voucher -> {
            Long voucherId = voucher.getId();
            SeckillVoucher seckillVoucher = Db.lambdaQuery(SeckillVoucher.class)
                    .eq(SeckillVoucher::getVoucherId, voucherId)
                    .one();
            if(seckillVoucher != null){
                BeanUtil.copyProperties(seckillVoucher, voucher);
            }
        });
        return voucherList;
    }
}
