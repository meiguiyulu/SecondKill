package com.liuyj.secondkill.service.impl;

import com.liuyj.secondkill.pojo.Goods;
import com.liuyj.secondkill.mapper.GoodsMapper;
import com.liuyj.secondkill.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liuyj.secondkill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author LiuYunJie
 * @since 2022-01-23
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    /*获取商品列表*/
    @Override
    public List<GoodsVo> findGoodsVo() {
        return goodsMapper.findGoodsVo();
    }

    /*获取商品详情*/
    @Override
    public GoodsVo findGoodsVoById(Long goodsId) {
        return goodsMapper.findGoodsVoById(goodsId);
    }
}
