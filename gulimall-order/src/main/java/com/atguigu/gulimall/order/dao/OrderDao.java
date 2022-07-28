package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:20:54
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
