package com.liuyj.secondkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.vo.LoginVo;
import com.liuyj.secondkill.vo.ResponseBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-18
 */
public interface IUserService extends IService<User> {

    /**
     * 实现登陆功能
    * */
    ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request,
                         HttpServletResponse response);

    /**
     * 根据cookie获取用户
     */
    User getUserByCookie(String userTicket, HttpServletRequest request,
                         HttpServletResponse response);
}
