package com.liuyj.secondkill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuyj.secondkill.exception.GlobalException;
import com.liuyj.secondkill.mapper.UserMapper;
import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.service.IUserService;
import com.liuyj.secondkill.utils.CookieUtil;
import com.liuyj.secondkill.utils.MD5Util;
import com.liuyj.secondkill.utils.UUIDUtil;
import com.liuyj.secondkill.utils.ValidatorUtil;
import com.liuyj.secondkill.vo.LoginVo;
import com.liuyj.secondkill.vo.ResponseBean;
import com.liuyj.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-18
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 登录功能
     */
    @Override
    public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request,
                                HttpServletResponse response) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();

        // 根据手机号获取用户
        User user = userMapper.selectById(mobile);
        if (null == user) {
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
        }

        // 判断密码是否正确
        if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
            throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
        }

        // 生成cookie
        String ticket = UUIDUtil.uuid();
/*        放在session中
        request.getSession().setAttribute(ticket, user);*/
        /*将用户信息存入redis中*/
        redisTemplate.opsForValue().set("user:" + ticket, user);
        CookieUtil.setCookie(request, response, "userTicket", ticket);

        return ResponseBean.success();
    }

    /**
     * 根据cookie获取用户
     *
     * @param userTicket
     * @return
     */
    @Override
    public User getUserByCookie(String userTicket, HttpServletRequest request,
                                HttpServletResponse response) {
        if (StringUtils.isEmpty(userTicket)) {
            return null;
        }
        User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
        if (user != null) {
            CookieUtil.setCookie(request, response, "userTicket", userTicket);
        }
        return user;
    }

    /**
     * 更新密码
     * */
    @Override
    public ResponseBean updatePassword(String userTicket, String password,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        User user = getUserByCookie(userTicket, request, response);
        if (user == null) {
            throw new GlobalException(ResponseBeanEnum.MOBILE_NOT_EXIST);
        }
        user.setPassword(MD5Util.fromPasswordToDBPassword(password, user.getSalt()));
        int i = userMapper.updateById(user);
        if (i == 1) {
            /*删除Redis*/
            redisTemplate.delete("user:" + userTicket);
            return ResponseBean.success();
        }

        return ResponseBean.error(ResponseBeanEnum.PASSWORD_UPDATE_FAIL);
    }
}
