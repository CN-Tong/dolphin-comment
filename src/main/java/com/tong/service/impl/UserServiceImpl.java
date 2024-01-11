package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.PrefixConstants;
import com.tong.constant.RedisConstants;
import com.tong.exception.BusinessException;
import com.tong.pojo.dto.LoginFormDTO;
import com.tong.pojo.dto.UserDTO;
import com.tong.pojo.entity.User;
import com.tong.mapper.UserMapper;
import com.tong.service.IUserService;
import com.tong.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCodeThroughSession(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，抛出异常
            throw new BusinessException("手机号格式错误！");
        }
        // 2.符合，生成验证码（hutool工具包）
        String code = RandomUtil.randomNumbers(6);
        // 3.保存验证码到Session
        session.setAttribute(PrefixConstants.SESSION_CODE, code);
        // 4.（模拟）发送验证码
        log.info("发送短信验证码成功，验证码：{}", code);
    }

    @Override
    public String sendCodeThroughRedis(String phone, HttpSession session) {
        // 1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 不符合，抛出异常
            throw new BusinessException("手机号格式错误！");
        }
        // 2.符合，生成验证码（hutool工具包）
        String code = RandomUtil.randomNumbers(6);
        // 3.保存验证码到Redis,5min内有效
        stringRedisTemplate.opsForValue()
                .set(RedisConstants.LOGIN_CODE_KEY + phone, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 4.（模拟）发送验证码
        log.info("发送短信验证码成功，验证码：{}", code);
        return code;
    }


    @Override
    public void loginThroughSession(LoginFormDTO loginFormDTO, HttpSession session) {
        String phone = loginFormDTO.getPhone();
        // 1.校验手机号和验证码
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BusinessException("手机号格式错误！");
        }
        // 分别取出Session和前端的验证码
        Object cacheCode = session.getAttribute(PrefixConstants.SESSION_CODE);
        String code = loginFormDTO.getCode();
        // Session中不存在验证码，或俩验证码不一致，抛出异常
        if (cacheCode == null || !cacheCode.toString().equals(code)) {
            throw new BusinessException("验证码错误");
        }
        // 2.验证码一致，根据手机号查询用户
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        // 3.判断用户是否存在
        if (user == null) {
            // 3.1不存在，创建新用户，保存用户到数据库（自动注册）
            user = createUserWithPhone(phone);
            save(user);
        }
        // 4.不管是否存在，都需要保存用户到Session
        // 用户信息脱敏
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        session.setAttribute(PrefixConstants.SESSION_USER, userDTO);
    }

    @Override
    public String loginThroughRedis(LoginFormDTO loginFormDTO, HttpSession session) {
        String phone = loginFormDTO.getPhone();
        // 1.校验手机号和验证码
        if (RegexUtils.isPhoneInvalid(phone)) {
            throw new BusinessException("手机号格式错误！");
        }
        // 分别取出Redis和前端的验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code = loginFormDTO.getCode();
        // Redis中不存在验证码，或俩验证码不一致，抛出异常
        if (cacheCode == null || !cacheCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        // 2.验证码一致，根据手机号查询用户
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        // 3.判断用户是否存在
        if (user == null) {
            // 3.1不存在，创建新用户，保存用户到数据库（自动注册）
            user = createUserWithPhone(phone);
            save(user);
        }
        // 4.不管是否存在，都需要保存用户到Redis
        // 用户信息脱敏
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString();
        // bean转map
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create().setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 用Hash结构保存
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置有效期60min
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 返回token
        return token;
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(PrefixConstants.RANDOM_NICKNAME + RandomUtil.randomString(10));
        return user;
    }
}
