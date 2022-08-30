package com.atguigu.gulimall.coupon.service.impl;

import com.atguigu.common.to.MemberPriceTo;
import com.atguigu.common.to.SkuReduceTo;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.coupon.entity.MemberPriceEntity;
import com.atguigu.gulimall.coupon.entity.SkuLadderEntity;
import com.atguigu.gulimall.coupon.service.MemberPriceService;
import com.atguigu.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atguigu.gulimall.coupon.dao.SkuFullReductionDao;
import com.atguigu.gulimall.coupon.entity.SkuFullReductionEntity;
import com.atguigu.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存满减优惠信息
     * @param skuReduceTo
     */
    @Override
    public void saveInfo(SkuReduceTo skuReduceTo) {
        //满减信息
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReduceTo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReduceTo.getPriceStatus());
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            this.save(skuFullReductionEntity);
        }


        //阶梯价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtils.copyProperties(skuReduceTo,skuLadderEntity);
        skuLadderEntity.setAddOther(skuReduceTo.getCountStatus());
        ////TODO 折后价可在下单时计算
        if (skuLadderEntity.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }


        //会员价格信息
        List<MemberPriceTo> memberPriceToList = skuReduceTo.getMemberPriceToList();
        if (memberPriceToList != null && memberPriceToList.size() > 0) {
            List<MemberPriceEntity> collect = memberPriceToList.stream().map(item -> {
                MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                memberPriceEntity.setSkuId(skuReduceTo.getSkuId());
                memberPriceEntity.setMemberLevelId(item.getId());
                memberPriceEntity.setMemberPrice(item.getPrice());
                memberPriceEntity.setMemberLevelName(item.getName());
                return memberPriceEntity;
            }).filter(per -> per.getMemberPrice().compareTo(new BigDecimal("0") )== 1)
                    .collect(Collectors.toList());
            memberPriceService.saveBatch(collect);
        }

    }
}