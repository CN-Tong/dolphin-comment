package com.tong.controller;


import com.tong.pojo.dto.LoginFormDTO;
import com.tong.result.Result;
import com.tong.pojo.entity.UserInfo;
import com.tong.service.IUserInfoService;
import com.tong.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @PostMapping("code")
    @ApiOperation("发送手机验证码")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        log.info("发送手机验证码，phone：{}", phone);
        // TODO 发送短信验证码并保存验证码
        return Result.fail("功能未完成");
    }

    @PostMapping("/login")
    @ApiOperation("短信登录")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        log.info("短信登录，loginFormDTO：{}", loginFormDTO);
        // TODO 实现登录功能
        return Result.fail("功能未完成");
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result logout() {
        log.info("退出登录");
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    @ApiOperation("获取当前登录用户")
    public Result me() {
        log.info("获取当前登录用户");
        // TODO 获取当前登录的用户并返回
        return Result.fail("功能未完成");
    }

    @GetMapping("/info/{id}")
    @ApiOperation("根据用户id获取用户信息")
    public Result info(@PathVariable("id") Long userId) {
        log.info("根据用户id获取用户信息，userId：{}", userId);
        UserInfo info = userInfoService.getInfoByUserId(userId);
        return Result.ok(info);
    }
}
