package com.liuyj.secondkill.utils;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LYJ
 * @create 2022-01-19 21:16
 * 手机号码校验
 */
public class ValidatorUtil {

    // 手机号正则表达式
    private static final Pattern mobile_pattern =
            Pattern.compile("^1(?:3\\d|4[4-9]|5[0-35-9]|6[67]|7[013-8]|8\\d|9\\d)\\d{8}$");

    public static boolean isMobile(String mobile) {
        if (StringUtils.isEmpty(mobile)) {
            return false;
        }
        // 手机号码校验
        Matcher matcher = mobile_pattern.matcher(mobile);
        return matcher.matches();
    }
}
