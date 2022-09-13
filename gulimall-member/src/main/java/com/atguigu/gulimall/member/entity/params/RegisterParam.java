package com.atguigu.gulimall.member.entity.params;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class RegisterParam {


    private String userName;


    private String password;

    private String phone;

}
