package com.tong.service.impl;

import com.tong.pojo.entity.VoucherOrder;
import com.tong.mapper.VoucherOrderMapper;
import com.tong.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

}
