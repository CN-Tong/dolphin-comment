package com.tong.service;

import com.tong.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserInfoService extends IService<UserInfo> {

    UserInfo getInfoByUserId(Long userId);
}
