package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.constant.MessageConstants;
import com.hmdp.constant.SessionConstants;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    /**
     * 发送验证码
     *
     * @param phone 手机号
     * @return {@link Result}
     */
    @Override
    public Result sendCode(String phone, HttpSession session) {

        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(MessageConstants.PHONE_FORMAT_INVALID);
        }
        String code = RandomUtil.randomNumbers(6);
        session.setAttribute(SessionConstants.VERIFY_CODE, code);
        log.info("验证码:{}", code);
        return Result.ok();

    }
    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     * @return {@link Result}
     */
    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail(MessageConstants.PHONE_FORMAT_INVALID);
        }
        String sessionCode = (String) session.getAttribute(SessionConstants.VERIFY_CODE);
        if (code == null || !code.equals(sessionCode)) {
            return Result.fail(MessageConstants.VERIFY_CODE_INVALID);
        }
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPassword, phone));
        if (Objects.isNull(user)) {
            user = createUserWithPhone(phone);
        }
        session.setAttribute(SessionConstants.LOGIN_USER, BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }
    /**
     * 创建用户
     *
     * @param phone 手机号
     * @return {@link User}
     */
    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        this.save(user);
        return user;
    }
}