package com.atguigu.gulimall.auth.server.entity.params;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class RegisterParam {

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6, max = 12, message = "用户名长度为6到12个字符")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号码格式不正确")

    private String phone;
    @NotEmpty(message = "验证码不能为空")
    private String code;
}
