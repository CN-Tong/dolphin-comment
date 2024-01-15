package com.tong.service;

import com.tong.pojo.entity.SeckillVoucher;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ISeckillVoucherService extends IService<SeckillVoucher> {

    Long seckillVoucherById(Long voucherId);
}
