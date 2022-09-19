package com.atguigu.gulimall.cart.entity.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author dty
 * @date 2022/9/14
 * @dec 描述
 */
public class Cart {
    private List<CartItem> items;

    /**
     * 商品数量
     */
    private Integer countNum;

    /**
     * 商品种类
     */
    private Integer countType;

    private BigDecimal reduce;

    private BigDecimal totalAmount;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count =  0;
        if (this.items != null && this.items.size() > 0) {
            for (CartItem item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }



    public Integer getCountType() {
        int count =  0;
        if (this.items != null ) {
            count = this.items.size();
        }
        return count;
    }



    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = new BigDecimal("0");
        if (this.items != null && this.items.size() > 0) {
            for (CartItem item : this.items) {
                //返回的结果才是加之后的数据 ,选中的购物项才计算总价格·
                if (item.getCheck()) {
                    total = total.add(item.getTotalPrice());
                }

            }
        }
        return total;
    }

}
