package com.liuyj.secondkill.exception;

import com.liuyj.secondkill.vo.ResponseBean;
import com.liuyj.secondkill.vo.ResponseBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author LYJ
 * @create 2022-01-20 10:50'
 * 全局异常处理类
 */

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseBean ExceptionHandler(Exception e) {
        if (e instanceof GlobalException) {
            GlobalException ex = (GlobalException) e;
            return ResponseBean.error(ex.getResponseBeanEnum());
        } else if (e instanceof BindException){
            BindException ex = (BindException) e;
            ResponseBean responseBean = ResponseBean.error(ResponseBeanEnum.BIND_ERROR);
            responseBean.setMessage("参数校验异常: " +
                    ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return responseBean;
        }
        return ResponseBean.error(ResponseBeanEnum.ERROR);
    }

}
