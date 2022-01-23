package com.liuyj.secondkill.mapper;

import com.liuyj.secondkill.pojo.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuyj.secondkill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-23
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    /*获取商品列表*/
    List<GoodsVo> findGoodsVo();

    /*获取商品详情*/
    GoodsVo findGoodsVoById(Long goodsId);
}
