package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.entity.vo.MemberAddressVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberReceiveAddressDao;
import com.atguigu.gulimall.member.entity.MemberReceiveAddressEntity;
import com.atguigu.gulimall.member.service.MemberReceiveAddressService;


@Service("memberReceiveAddressService")
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressDao, MemberReceiveAddressEntity> implements MemberReceiveAddressService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberReceiveAddressEntity> page = this.page(
                new Query<MemberReceiveAddressEntity>().getPage(params),
                new QueryWrapper<MemberReceiveAddressEntity>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<MemberAddressVo> getAddressById(Long userId) {
        List<MemberReceiveAddressEntity> list = lambdaQuery().eq(MemberReceiveAddressEntity::getMemberId, userId).list();
        return list.stream().map(per -> {
            MemberAddressVo memberAddressVo = new MemberAddressVo();
            BeanUtils.copyProperties(per, memberAddressVo);
            return memberAddressVo;
        }).collect(Collectors.toList());
    }
}
