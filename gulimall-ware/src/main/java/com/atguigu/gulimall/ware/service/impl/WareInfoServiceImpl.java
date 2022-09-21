package com.atguigu.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.ware.entity.vo.FareVo;
import com.atguigu.gulimall.ware.entity.vo.MemberAddressVo;
import com.atguigu.gulimall.ware.feign.MemberFeignService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.WareInfoDao;
import com.atguigu.gulimall.ware.entity.WareInfoEntity;
import com.atguigu.gulimall.ware.service.WareInfoService;


/**
 * @author Administrator
 */
@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and( (w) -> w.like(WareInfoEntity::getName,key).or().like(WareInfoEntity::getAddress, key));
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


    /**
     * 获取运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {

        R info = memberFeignService.info(addrId);
        if (info.getCode() == 0) {
            FareVo fareVo = new FareVo();
            BigDecimal fare = new BigDecimal("0");
            MemberAddressVo addressVo = info.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
            });
            String phone = addressVo.getPhone();
            BigDecimal bigDecimal = new BigDecimal(phone.substring(phone.length() - 1));
            fare = fare.add(bigDecimal.subtract(new BigDecimal("10"))).add(new BigDecimal("10"));
            fareVo.setFare(fare);
            fareVo.setAddress(addressVo);
            return fareVo;
        }
        return null;
    }


}
