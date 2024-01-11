package com.tong.service;

import com.tong.pojo.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IVoucherService extends IService<Voucher> {

    void saveSeckillVoucher(Voucher voucher);

    List<Voucher> listVoucherByShopId(Long shopId);
}
