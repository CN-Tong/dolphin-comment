package com.tong.service;

import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IFollowService extends IService<Follow> {

    void follow(Long followUserId, boolean isFollow);

    boolean isFollow(Long followUserId);

    List<UserDTO> commonFollow(Long id);
}
