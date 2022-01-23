package com.liuyj.secondkill.exception;

import com.liuyj.secondkill.vo.ResponseBeanEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LYJ
 * @create 2022-01-20 10:49
 * 全局异常
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalException extends RuntimeException{

    private ResponseBeanEnum responseBeanEnum;

}
