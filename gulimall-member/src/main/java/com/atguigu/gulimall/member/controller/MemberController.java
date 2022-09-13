package com.atguigu.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.gulimall.member.entity.params.LoginParam;
import com.atguigu.gulimall.member.entity.params.RegisterParam;
import com.atguigu.gulimall.member.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 会员
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:10:02
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("coupons")
    public R test(){
        return couponFeignService.memberCoupon();
    }



    @PostMapping("/register")
    public R register(@RequestBody RegisterParam registerParam) {

        return memberService.register(registerParam);
    }


    @PostMapping("/login")
    R login(@RequestBody LoginParam loginParam) {
        MemberEntity member = memberService.login(loginParam);
        if (null == member) {
            return R.error(BizExceptionEnum.USER_PASSWORD_ERROR.getCode(), BizExceptionEnum.USER_PASSWORD_ERROR.getMsg());
        } else {
            return R.ok().put("data", member);
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
