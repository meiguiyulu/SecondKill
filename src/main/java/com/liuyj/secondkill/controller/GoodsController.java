package com.liuyj.secondkill.controller;

import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.service.IGoodsService;
import com.liuyj.secondkill.service.impl.UserServiceImpl;
import com.liuyj.secondkill.vo.DetailsVo;
import com.liuyj.secondkill.vo.GoodsVo;
import com.liuyj.secondkill.vo.ResponseBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author LYJ
 * @create 2022-01-20 19:53
 * 判断用户是否正确登录
 * 若正确登录，则跳到商品页面
 * 否则，回到登陆页面
 */

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private UserServiceImpl userService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

    /*
     * 跳转到商品页面
     * */
    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request,
                         HttpServletResponse response) {
        /*从Redis获取页面，如果不为空，直接返回页面*/
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
//        return "goodsList";

        /*若页面是空，则手动渲染页面，并将其存入Redis返回*/
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    /*
     *  功能描述: 跳转商品详情页
     * */
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail2(Model model, User user, @PathVariable Long goodsId,
                           HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        /*Redis中获取页面，如果不为空，直接返回页面*/
        String html = (String) valueOperations.get("goodsDetail" + goodsId);
        if (!StringUtils.isEmpty(html)) {
            return html;
        }

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

        /*Redis中获取页面，如果为空，thymeleaf渲染页面并且存放到Redis中*/
        WebContext context = new WebContext(request, response, request.getServletContext(), request.getLocale(),
                model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", context);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsDetail" + goodsId, html, 60, TimeUnit.SECONDS);
        }
        return html;
//        return "goodsDetail";
    }

    /**
     * 功能描述: 跳转商品详情页
     *
     * @param:
     * @return:
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @since: 1.0.0
     * @Author:zhoubin
     */
    @RequestMapping("/detail/{goodsId}")
    @ResponseBody
    public ResponseBean toDetail(User user, @PathVariable Long goodsId) {
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        //秒杀还未开始
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //	秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailsVo detailVo = new DetailsVo();
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return ResponseBean.success(detailVo);
    }

}
