package com.atguigu.gulimall.cart.entity.to;

import lombok.Data;

/**
 * @author dty
 * @date 2022/9/15
 * @dec 描述
 */
@Data
public class UserInfoTo {

    private Long userId;

    private String userKey;

    private boolean temUser = false;
}
