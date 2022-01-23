package com.liuyj.secondkill.service.impl;

import com.liuyj.secondkill.pojo.Order;
import com.liuyj.secondkill.mapper.OrderMapper;
import com.liuyj.secondkill.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-23
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
