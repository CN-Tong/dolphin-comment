package com.tong.service;

import com.tong.pojo.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IFollowService extends IService<Follow> {

    void follow(Long followUserId, boolean isFollow);

    boolean isFollow(Long followUserId);
}
