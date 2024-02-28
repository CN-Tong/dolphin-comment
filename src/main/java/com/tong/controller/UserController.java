package com.tong.controller;


import com.tong.pojo.dto.LoginFormDTO;
import com.tong.pojo.dto.UserDTO;
import com.tong.result.Result;
import com.tong.pojo.entity.UserInfo;
import com.tong.service.IUserInfoService;
import com.tong.service.IUserService;
import com.tong.utils.UserHolder;
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
        String code = userService.sendCodeThroughRedis(phone, session);
        return Result.ok(code);
    }

    @PostMapping("/login")
    @ApiOperation("短信登录")
    public Result login(@RequestBody LoginFormDTO loginFormDTO, HttpSession session) {
        log.info("短信登录，loginFormDTO：{}", loginFormDTO);
        String token = userService.loginThroughRedis(loginFormDTO, session);
        return Result.ok(token);
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result logout() {
        log.info("退出登录");
        UserHolder.removeUser();
        return Result.ok();
    }

    @GetMapping("/me")
    @ApiOperation("获取当前登录用户")
    public Result me() {
        log.info("获取当前登录用户");
        return Result.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    @ApiOperation("根据用户id获取用户信息")
    public Result info(@PathVariable("id") Long userId) {
        log.info("根据用户id获取用户信息，userId：{}", userId);
        UserInfo info = userInfoService.getInfoByUserId(userId);
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户")
    public Result getUserById(@PathVariable("id") Long id){
        log.info("根据id查询用户，id：{}", id);
        UserDTO userDTO = userService.getUserById(id);
        return Result.ok(userDTO);
    }
}
