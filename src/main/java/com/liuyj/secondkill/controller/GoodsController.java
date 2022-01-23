package com.liuyj.secondkill.controller;

import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.service.IGoodsService;
import com.liuyj.secondkill.service.impl.UserServiceImpl;
import com.liuyj.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author LYJ
 * @create 2022-01-20 19:53
 * 判断用户是否正确登录
 *      若正确登录，则跳到商品页面
 *      否则，回到登陆页面
 */

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private IGoodsService goodsService;

    /*
    * 跳转到商品页面
    * */
    @RequestMapping("/toList")
    public String toList(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());

        return "goodsList";
    }

    /*
    *  功能描述: 跳转商品详情页
    * */
    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user,
                           @PathVariable Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        /*秒杀状态*/
        int secKillStatus = 0;
        /*秒杀倒计时*/
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            /*秒杀还未开始*/
            remainSeconds = ((int) (nowDate.getTime() - startDate.getTime())) / 1000;
        } else if (nowDate.after(endDate)) {
            /*秒杀已经结束*/
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            /*秒杀进行中*/
            secKillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVo);
        return "goodsDetail";
    }

}
