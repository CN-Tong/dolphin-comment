package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.pojo.entity.User;
import com.tong.mapper.UserMapper;
import com.tong.service.IUserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
