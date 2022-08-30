package com.atguigu.gulimall.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author dty
 * @date 2022/8/30
 * @dec 描述
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
class User{
    private String name;
    private Integer age;
    private String gender;
}
