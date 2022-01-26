package com.liuyj.secondkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuyj.secondkill.pojo.Order;
import com.liuyj.secondkill.pojo.SeckillOrder;
import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.service.IGoodsService;
import com.liuyj.secondkill.service.IOrderService;
import com.liuyj.secondkill.service.ISeckillOrderService;
import com.liuyj.secondkill.vo.GoodsVo;
import com.liuyj.secondkill.vo.ResponseBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.IntBuffer;

/**
 * @author LYJ
 * @create 2022-01-24 9:25
 * 秒杀功能实现
 */

@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService secKillOrderService;
    @Autowired
    private IOrderService orderService;

    /*简化：跳转到订单页面*/
    @RequestMapping("doSeckill")
    public String doSeckill(Model model, User user,
                            Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        /**
         * 这里再次查看商品的库存原因在于前端页面上的库存数量很容易通过F12被修改
         * */
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        if (goodsVo.getStockCount() < 1) {
            model.addAttribute("errmsg",
                    ResponseBeanEnum.EMPTY_STOCK.getMessage());
            return "secKillFail";
        }

        /*查看是否重复抢购*/
        SeckillOrder seckillOrder = secKillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                .eq("user_id", user.getId())
                .eq("goods_id", goodsVo.getId()));
        if (seckillOrder != null) {
            model.addAttribute("errmsg", ResponseBeanEnum.REPEATE_ERROR.getMessage());
            return "secKillFail";
        }
        Order order = orderService.secKill(user, goodsVo);
        model.addAttribute("order", order);
        model.addAttribute("goods", goodsVo);
        return "orderDetail";
    }

}
