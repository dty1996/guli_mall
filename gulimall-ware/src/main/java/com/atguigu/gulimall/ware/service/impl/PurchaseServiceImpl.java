package com.atguigu.gulimall.ware.service.impl;

import com.atguigu.gulimall.ware.entity.param.DoneParam;
import com.atguigu.gulimall.ware.entity.param.DoneResult;
import com.atguigu.gulimall.ware.entity.param.MergeParam;
import com.atguigu.gulimall.ware.entity.PurchaseDetailEntity;
import com.atguigu.gulimall.ware.enums.PurchaseDetailEnum;
import com.atguigu.gulimall.ware.enums.PurchaseStatusEnum;
import com.atguigu.gulimall.ware.service.PurchaseDetailService;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.ware.dao.PurchaseDao;
import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDao purchaseDao;

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and( w -> {
                w.like(PurchaseEntity::getId, key).or().like(PurchaseEntity::getAssigneeName, key);
            });
        }
        String status = (String) params.get("status");
        if (StringUtils.isNotEmpty(status)) {
            Integer statusInt = Integer.parseInt(status);
            queryWrapper.eq(PurchaseEntity::getStatus, statusInt);
        }

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryUnReceivePage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> queryWrapper = new LambdaQueryWrapper<>();
        Integer status = PurchaseStatusEnum.NEW.getStatus();
        queryWrapper.eq(PurchaseEntity::getStatus, status);
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    /**
     * 合并采购单
     * @param mergeparam
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(MergeParam mergeparam) {
        Long purchaseId = mergeparam.getPurchaseId();
        //没有选择采购单新建
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setPriority(0);
            purchaseEntity.setStatus(PurchaseStatusEnum.NEW.getStatus());
            purchaseDao.insert(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        List<Long> items = mergeparam.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity purchaseDetail = new PurchaseDetailEntity();
            purchaseDetail.setId(i);
            purchaseDetail.setPurchaseId(finalPurchaseId);
            purchaseDetail.setStatus(PurchaseDetailEnum.DISTRIBUTED.getStatus());
            return purchaseDetail;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long[] purchaseIds) {

        //改变采购单信息
        List<PurchaseEntity> collect = Arrays.stream(purchaseIds).map(this::getById
        ).filter(item ->
                PurchaseStatusEnum.NEW.getStatus().equals(item.getStatus()) ||  PurchaseStatusEnum.DISTRIBUTED.getStatus().equals(item.getStatus())
        ).map(per -> {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setId(per.getId());
            purchaseEntity.setStatus(PurchaseStatusEnum.RECEIVED.getStatus());
            return purchaseEntity;
        }).collect(Collectors.toList());
        updateBatchById(collect);

        //改变采购项信息
        Arrays.stream(purchaseIds).forEach(purchaseId -> {
            List<PurchaseDetailEntity> details = purchaseDetailService.queryDetailsByPurchaseId(purchaseId);
            details.forEach(detail -> {
                detail.setStatus(PurchaseDetailEnum.PURCHASEING.getStatus());
            });
            purchaseDetailService.updateBatchById(details);
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void done(DoneParam doneParam) {
        Long id = doneParam.getId();

        boolean flag = true;
        List<DoneResult> items = doneParam.getItems();
        ArrayList<PurchaseDetailEntity> details = new ArrayList<>();
        for (DoneResult item : items) {
            PurchaseDetailEntity detail = new PurchaseDetailEntity();
            if (PurchaseDetailEnum.COMPLETED.getStatus().equals(item.getStatus())) {

                detail.setId(item.getItemId());
                detail.setStatus(PurchaseDetailEnum.COMPLETED.getStatus());
                PurchaseDetailEntity bId = purchaseDetailService.getById(item.getItemId());
                //成功增加库存
                wareSkuService.addStock(bId);
            } else {
                flag = false;
                detail.setId(item.getItemId());
                detail.setStatus(PurchaseDetailEnum.FAILED.getStatus());
            }
            details.add(detail);
        }
        purchaseDetailService.updateBatchById(details);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? PurchaseStatusEnum.COMPLETED.getStatus() : PurchaseStatusEnum.FAILED.getStatus());
        this.updateById(purchaseEntity);
    }
}
