package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.pojo.dto.LoginFormDTO;
import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.User;

import javax.servlet.http.HttpSession;

public interface IUserService extends IService<User> {

    void sendCodeThroughSession(String phone, HttpSession session);

    String sendCodeThroughRedis(String phone, HttpSession session);

    void loginThroughSession(LoginFormDTO loginFormDTO, HttpSession session);

    String loginThroughRedis(LoginFormDTO loginFormDTO, HttpSession session);

    UserDTO getUserById(Long id);
}
