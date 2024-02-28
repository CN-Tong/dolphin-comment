package com.tong.controller;


import com.tong.result.Result;
import com.tong.service.IFollowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/follow")
@Api(tags = "关注相关接口")
@Slf4j
public class FollowController {

    @Resource
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    @ApiOperation("尝试关注/取关")
    public Result follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") boolean isFollow){
        log.info("尝试关注/取关，followUserId：{}，isFollow：{}", followUserId, isFollow);
        followService.follow(followUserId, isFollow);
        return Result.ok();
    }

    @GetMapping("/or/not/{id}")
    @ApiOperation("查询是否关注了用户")
    public Result isFollow(@PathVariable("id") Long followUserId){
        log.info("尝试关注/取关，followUserId：{}", followUserId);
        boolean isFollow = followService.isFollow(followUserId);
        return Result.ok(isFollow);
    }
}
