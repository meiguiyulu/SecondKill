package com.liuyj.secondkill.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author LYJ
 * @create 2022-01-18 16:21
 */

@Controller
@RequestMapping("/demo")
public class DemoControl {


    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("name","测试");
        return "hello";
    }

}
