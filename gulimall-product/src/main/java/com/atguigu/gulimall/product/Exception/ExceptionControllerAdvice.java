package com.atguigu.gulimall.product.Exception;

import com.atguigu.common.exception.BizExceptionEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dty
 * @date 2022/8/16
 * @dec 集中处理所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.atguigu.gulimall.product.controller")
public class ExceptionControllerAdvice {

    /**
     * 处理校验异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public R handlerValidException(MethodArgumentNotValidException e){
        log.error("数据校验出现问题：{}， 异常类型：{}", e.getMessage(), e.getClass());
        Map<String, String> map = new HashMap<>();
        BindingResult bindingResult =e.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach( (per) ->{
            //错误信息
            String defaultMessage = per.getDefaultMessage();
            //出错属性名
            String field = per.getField();
            map.put(field, defaultMessage);
        });
        return R.error(BizExceptionEnum.VALID_EXCEPTION.getCode(),BizExceptionEnum.VALID_EXCEPTION.getMsg()).put("data",map);

    }

    /**
     * 所有异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Throwable.class)
    public R handlerException(Throwable e){
        log.error("出现异常：{}， 异常类型：{}", e.getMessage(), e.getClass());
        return R.error();

    }
}
