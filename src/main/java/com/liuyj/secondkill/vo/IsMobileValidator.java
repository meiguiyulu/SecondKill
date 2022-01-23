package com.liuyj.secondkill.vo;

import com.liuyj.secondkill.utils.ValidatorUtil;
import com.liuyj.secondkill.validator.IsMobile;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author LYJ
 * @create 2022-01-20 10:33
 * 手机号校验规则
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {

    /*
    *  required记录手机号是否必填
    * */
    private boolean required = false;

    @Override
    public void initialize(IsMobile constraintAnnotation) {
        required = constraintAnnotation.required();
//        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (required) {
            /*
            必填
            */
            return ValidatorUtil.isMobile(s);
        } else {
            /*
            非必填
            * */
            if (StringUtils.isEmpty(s)) {
                return true;
            } else {
                return ValidatorUtil.isMobile(s);
            }
        }
    }
}
