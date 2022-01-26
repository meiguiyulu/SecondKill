package com.liuyj.secondkill.service;

import com.liuyj.secondkill.pojo.Order;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuyj.secondkill.pojo.User;
import com.liuyj.secondkill.vo.GoodsVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-23
 */
public interface IOrderService extends IService<Order> {

    /*秒杀功能*/
    Order secKill(User user, GoodsVo model);
}
