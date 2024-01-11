package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.pojo.dto.LoginFormDTO;
import com.tong.pojo.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    void sendCode(String phone, HttpSession session);

    void login(LoginFormDTO loginFormDTO, HttpSession session);
}
