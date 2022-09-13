package com.atguigu.gulimall.member.service;

import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.entity.params.LoginParam;
import com.atguigu.gulimall.member.entity.params.RegisterParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:10:02
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    R register(RegisterParam registerParam);

    MemberEntity login(LoginParam loginParam);
}

