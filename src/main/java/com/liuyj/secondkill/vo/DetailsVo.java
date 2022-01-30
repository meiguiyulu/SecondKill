package com.liuyj.secondkill.vo;

import com.liuyj.secondkill.pojo.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详情返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailsVo {

    private User user;

    private GoodsVo goodsVo;

    private int secKillStatus;

    private int remainSeconds;

}
