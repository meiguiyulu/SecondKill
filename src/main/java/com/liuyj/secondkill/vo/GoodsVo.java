package com.liuyj.secondkill.vo;

import com.liuyj.secondkill.pojo.Goods;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author LYJ
 * @create 2022-01-23 11:01
 * 商品返回对象
 *      因为单个商品表不足以展示所需要的信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo extends Goods {

    /**
     * 因为此类继承自Goods 所以Goods的参数就不必写了
     */

    /**
     * 秒杀价
     */
    private BigDecimal seckillPrice;
    /**
     * 库存数量
     */
    private Integer stockCount;
    /**
     * 秒杀开始时间
     */
    private Date startDate;
    /**
     * 秒杀结束时间
     */
    private Date endDate;


}
