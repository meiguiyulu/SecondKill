package com.liuyj.secondkill.controller;

import com.liuyj.secondkill.service.IUserService;
import com.liuyj.secondkill.vo.LoginVo;
import com.liuyj.secondkill.vo.ResponseBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author LYJ
 * @create 2022-01-19 18:09
 */

@Controller
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    IUserService iUserService;

    /**
     * 跳转到登陆页面
     * */
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }


    /**
     * 登录功能
     * */
    @RequestMapping("/doLogin")
    @ResponseBody
    public ResponseBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request,
                                HttpServletResponse response) {
        return iUserService.doLogin(loginVo, request, response);
    }

}
