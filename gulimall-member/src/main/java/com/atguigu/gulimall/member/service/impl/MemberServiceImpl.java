package com.atguigu.gulimall.member.service.impl;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.member.constants.MemConstant;
import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.entity.params.LoginParam;
import com.atguigu.gulimall.member.entity.params.RegisterParam;
import com.atguigu.gulimall.member.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R register(RegisterParam registerParam) {
        //check username
        Integer count = lambdaQuery().eq(MemberEntity::getUsername, registerParam.getUserName()).count();
        if (count > 0) {
            return R.error(BizExceptionEnum.USER_EXIST_EXCEPTION.getCode(), BizExceptionEnum.USER_EXIST_EXCEPTION.getMsg());
        }

        //check phone
        Integer mobile = lambdaQuery().eq(MemberEntity::getMobile, registerParam.getPhone()).count();
        if (mobile > 0) {
            return R.error(BizExceptionEnum.PHONE_EXIST_EXCEPTION.getCode(), BizExceptionEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(registerParam.getUserName());
        memberEntity.setMobile(registerParam.getPhone());

        //使用spring框架的加盐算法
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodePassword = encoder.encode(registerParam.getPassword());
        memberEntity.setPassword(encodePassword);
        //设置默认会员等级
        MemberLevelEntity memberLevel = memberLevelService.lambdaQuery().eq(MemberLevelEntity::getDefaultStatus, MemConstant.DEFAULT_LEVEL_STATUS).one();
        memberEntity.setLevelId(memberLevel.getId());
        memberEntity.setCreateTime(new Date());

        save(memberEntity);
        return R.ok();
    }

    @Override
    public MemberEntity login(LoginParam loginParam) {
        String loginacct = loginParam.getLoginacct();
        String password = loginParam.getPassword();
        MemberEntity member = lambdaQuery().eq(MemberEntity::getMobile, loginacct).or().eq(MemberEntity::getUsername, loginacct).one();
        if (member != null) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
                return null;
            }
            return member;
        }
        return null;
    }
}
