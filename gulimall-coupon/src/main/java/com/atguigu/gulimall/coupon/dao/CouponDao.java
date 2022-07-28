package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 15:20:18
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
