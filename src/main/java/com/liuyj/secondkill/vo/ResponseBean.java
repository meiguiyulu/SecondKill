package com.liuyj.secondkill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LYJ
 * @create 2022-01-19 18:15
 * 公共返回对象
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean {
    private long code;
    private String message;
    private Object object;

    /**
    * 成功返回对象
    * */
    public static ResponseBean success(){
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(),
                ResponseBeanEnum.SUCCESS.getMessage(), null);
    }

    public static ResponseBean success(Object obj) {
        return new ResponseBean(ResponseBeanEnum.SUCCESS.getCode(),
                ResponseBeanEnum.SUCCESS.getMessage(), obj);
    }

    /**
     * 失败
     * 这里传入一个枚举类型的原因子在于：
     * 1 成功都是200
     * 2 失败原因很多 各有不同 例如 404、500
     * */
    public static ResponseBean error(ResponseBeanEnum responseBeanEnum){
        return new ResponseBean(responseBeanEnum.getCode(),
                responseBeanEnum.getMessage(), null);
    }

    public static ResponseBean error(ResponseBeanEnum responseBeanEnum, Object obj){
        return new ResponseBean(responseBeanEnum.getCode(),
                responseBeanEnum.getMessage(), obj);
    }

}
