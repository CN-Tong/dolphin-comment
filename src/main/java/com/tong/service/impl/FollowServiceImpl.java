package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.Follow;
import com.tong.mapper.FollowMapper;
import com.tong.pojo.entity.User;
import com.tong.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void follow(Long followUserId, boolean isFollow) {
        Long currentUserId = UserHolder.getUser().getId();
        String key = RedisConstants.FOLLOWS_KEY + currentUserId;
        // 1.判断是关注还是取关
        if (isFollow) {
            // 2.若需要关注，新增
            Follow follow = new Follow();
            follow.setFollowUserId(followUserId);
            follow.setUserId(currentUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 保存到Redis
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.若需要取关，删除
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", currentUserId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
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

    @Override
    public List<UserDTO> commonFollow(Long id) {
        Long currentUserId = UserHolder.getUser().getId();
        String key1 = RedisConstants.FOLLOWS_KEY + currentUserId;
        String key2 = RedisConstants.FOLLOWS_KEY + id;
        // 求交集
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if(CollUtil.isEmpty(intersect)){
            return Collections.emptyList();
        }
        // 解析id
        List<Long> commonFollowIds = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 查询用户
        List<User> userList = Db.listByIds(commonFollowIds, User.class);
        List<UserDTO> userDTOList = BeanUtil.copyToList(userList, UserDTO.class);
        return userDTOList;
    }
}
