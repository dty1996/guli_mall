package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.entity.params.SearchParam;
import com.atguigu.gulimall.search.entity.vo.SearchResponseVo;

/**
 * @author dty
 * @date 2022/9/6
 * @dec 描述
 */
public interface MallSearchService {
    SearchResponseVo search(SearchParam searchParam);
}
