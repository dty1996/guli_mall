package com.atguigu.gulimall.auth.server.entity.params;

import lombok.Data;

@Data
public class LoginParam {
    private String loginacct;
    private String password;
}
