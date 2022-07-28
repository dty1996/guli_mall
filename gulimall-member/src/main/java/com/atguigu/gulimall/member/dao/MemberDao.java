package com.atguigu.gulimall.member.dao;

import com.atguigu.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:10:02
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
