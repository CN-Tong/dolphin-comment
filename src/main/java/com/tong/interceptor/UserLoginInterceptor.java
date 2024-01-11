package com.tong.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.tong.constant.PrefixConstants;
import com.tong.pojo.dto.UserDTO;
import com.tong.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class UserLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取Session中的用户
        HttpSession session = request.getSession();
        Object user = session.getAttribute(PrefixConstants.SESSION_USER);
        // 2.判断用户是否存在
        if (user == null) {
            // 不存在，拦截，响应401状态码
            response.setStatus(401);
            return false;
        }
        // 3.存在，保存用户信息到ThreadLocal
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        UserHolder.saveUser(userDTO);
        // 4.放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
