package com.tong.service.impl;

import com.tong.pojo.entity.UserInfo;
import com.tong.mapper.UserInfoMapper;
import com.tong.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    @Override
    public UserInfo getInfoByUserId(Long userId) {
        UserInfo userInfo = lambdaQuery()
                .eq(UserInfo::getUserId, userId)
                .one();
        return userInfo;
    }
}
