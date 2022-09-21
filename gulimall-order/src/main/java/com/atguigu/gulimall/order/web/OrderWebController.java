package com.atguigu.gulimall.order.web;

import com.atguigu.gulimall.order.entity.vo.OrderConfirmVo;
import com.atguigu.gulimall.order.entity.vo.SubmitOrderResponseVo;
import com.atguigu.gulimall.order.entity.vo.SubmitOrderVo;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

/**
 * @author Administrator
 */
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }


    @PostMapping("submitOrder")
    public String submitOrder(SubmitOrderVo submitOrderVo,Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(submitOrderVo);
        if (responseVo.getCode() == 0) {
            model.addAttribute("submitOrderResp", responseVo);
            return "pay";
        } else {
            String msg = "下单失败";
            switch (responseVo.getCode()) {
                case 1:
                    msg += "订单信息过期，请刷新重新提交";
                    break;
                case 2:
                    msg += "订单商品价格发生变化，请确认后再次提交";
                    break;
                case 3:
                    msg += "商品库存不足";
                    break;
                default:

            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
            // }
            //}catch (Exception e){
            //if(e instanceof NoStockException){
            //String msg = ((NoStockException) e).getMessage();
            //redirectAttributes.addFlashAttribute("msg",msg);
        }
        //return "redirect:http://order.gulimall.com/toTrade";
        //}
    }



}
