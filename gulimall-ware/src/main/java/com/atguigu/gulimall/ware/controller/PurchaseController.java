package com.atguigu.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;

import com.atguigu.gulimall.ware.entity.param.DoneParam;
import com.atguigu.gulimall.ware.entity.param.MergeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gulimall.ware.entity.PurchaseEntity;
import com.atguigu.gulimall.ware.service.PurchaseService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.R;



/**
 * 采购信息
 *
 * @author dty
 * @email 1451069487@qq.com
 * @date 2022-07-28 16:39:38
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 查询未分配采购单
     */
    @RequestMapping("unreceive/list")
    public R unReceiveList(@RequestParam Map<String, Object> params) {
        PageUtils page = purchaseService.queryUnReceivePage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 合并采购寻求
     * @param mergeparam
     * @return
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeParam mergeparam) {
        purchaseService.merge(mergeparam);
        return R.ok();
    }

    /**
     * 接收采购单
     * @param purchaseIds
     * @return
     */
    @PostMapping("/receive")
    public R receive(@RequestBody Long[] purchaseIds) {
        purchaseService.receive(purchaseIds);
        return R.ok();
    }

    @PostMapping("/done")
    public R done(@RequestBody DoneParam doneParam) {
        purchaseService.done(doneParam);
        return R.ok();
    }


}
