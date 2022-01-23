package com.liuyj.secondkill.vo;

import com.liuyj.secondkill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author LYJ
 * @create 2022-01-19 20:33
 * 登录参数: 手机号 密码
 */

@Data
public class LoginVo {
    @NotNull
    @IsMobile
    private String mobile;

    @NotNull
    @Length(min = 32)  // 长度最小32号
    private String password;
}
