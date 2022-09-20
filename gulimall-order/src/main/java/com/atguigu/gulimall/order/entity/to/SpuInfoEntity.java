package com.atguigu.gulimall.order.entity.to;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * spu信息
 *
 * @author dty
 * @email dty@gmail.com
 * @date 2022-07-27 22:27:51
 */
@Data
public class SpuInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商品id
	 */
	private Long id;
	/**
	 * 商品名称
	 */
	private String spuName;
	/**
	 * 商品描述
	 */
	private String spuDescription;
	/**
	 * 所属分类id
	 */
	private Long catalogId;
	/**
	 * 品牌id
	 */
	private Long brandId;
	/**
	 *
	 */
	private BigDecimal weight;
	/**
	 * 上架状态[0 - 下架，1 - 上架]
	 */
	private Integer publishStatus;
	/**
	 *
	 */
	private Date createTime;
	/**
	 *
	 */
	private Date updateTime;

}