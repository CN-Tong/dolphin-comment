package com.tong.service.impl;

import com.tong.pojo.entity.Follow;
import com.tong.mapper.FollowMapper;
import com.tong.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

}
