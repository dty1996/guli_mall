package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.entity.vo.MemberAddressVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;

import java.util.List;
import java.util.Map;

/**
 * 会员收货地址
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:10:01
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddressEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<MemberAddressVo> getAddressById(Long userId);
}

