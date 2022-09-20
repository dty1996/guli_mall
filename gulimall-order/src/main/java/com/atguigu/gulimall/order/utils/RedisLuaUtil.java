package com.atguigu.gulimall.order.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * redis lua脚本工具类
 * @author Administrator
 */
@Component
public class RedisLuaUtil {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger log = LoggerFactory.getLogger(RedisLuaUtil.class);

    private static final String LUA_SCRIPT_CAD = "local current = redis.call('get', KEYS[1]);" +
            "if current == false then " +
            "    return nil;" +
            "end " +
            "if current == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]);" +
            "else " +
            "    return 0;" +
            "end";



    /**
     * 比较并删除
     * @param key
     * @param expect
     * @return 当key不存在时返回null;key存在,当前值=期望值时,删除key;key存在,当前值!=期望值时,返回0;
     */
    public Long compareAndDelete(String key,String expect){
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT_CAD,Long.class);
        Long result = null;
        try {
            result = stringRedisTemplate.execute(redisScript,keyList,expect);
        } catch (Exception e) {
            log.error("cad异常",e);
        }
        return result;
    }

}
