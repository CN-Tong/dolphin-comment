package com.tong.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.pojo.entity.Follow;
import com.tong.mapper.FollowMapper;
import com.tong.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.utils.UserHolder;
import org.springframework.stereotype.Service;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Override
    public void follow(Long followUserId, boolean isFollow) {
        Long currentUserId = UserHolder.getUser().getId();
        // 1.判断是关注还是取关
        if (isFollow) {
            // 2.若需要关注，新增
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(currentUserId);
            save(follow);
        } else {
            // 3.若需要取关，删除
            remove(new QueryWrapper<Follow>()
                    .eq("user_id", currentUserId).eq("follow_user_id", followUserId));
        }
    }

    @Override
    public boolean isFollow(Long followUserId) {
        Long currentUserId = UserHolder.getUser().getId();
        // 1.查询是否关注
        Long count = lambdaQuery()
                .eq(Follow::getUserId, currentUserId).eq(Follow::getFollowUserId, followUserId).count();
        // 2.判断是否关注
        return count > 0;
    }
}
