package com.liuyj.secondkill.service;

import com.liuyj.secondkill.pojo.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liuyj.secondkill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-23
 */
public interface IGoodsService extends IService<Goods> {

    /*获取商品列表*/
    List<GoodsVo> findGoodsVo();

    /*获取商品详情*/
    GoodsVo findGoodsVoById(Long goodsId);
}
