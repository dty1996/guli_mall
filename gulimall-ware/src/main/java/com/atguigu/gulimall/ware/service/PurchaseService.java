package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.ware.entity.param.DoneParam;
import com.atguigu.gulimall.ware.entity.param.MergeParam;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:39:38
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryUnReceivePage(Map<String, Object> params);

    void merge(MergeParam mergeparam);

    void receive(Long[] purchaseIds);

    void done(DoneParam doneParam);
}

