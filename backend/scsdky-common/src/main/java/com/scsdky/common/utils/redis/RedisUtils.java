package com.scsdky.common.utils.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * @author tubo
 * @date 2023/03/14
 */
@Component
public class RedisUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, Integer time, TimeUnit timeUnit){
        stringRedisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    public void set(String key, String value){
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String get(String key){
        return stringRedisTemplate.opsForValue().get(key);
    }

/*
    public List<String> getArrayStr(String likeKey){
        Set<String> keys = stringRedisTemplate.keys(likeKey + "*");
        if(CollectionUtils.isNotEmpty(keys)){
            return stringRedisTemplate.opsForValue().multiGet(keys);
        }
        return null;
    }
*/



    public void delete(String key){
        stringRedisTemplate.delete(key);
    }

    public void deleteKeys(Set<String> keys){
        stringRedisTemplate.delete(keys);
    }

    public void incrementScore(String sortedSetName,String key){
        // x 存在的就把分数加x(x可自行设定)，不存在就创建一个分数为1的成员
        double x = 1.0;
        stringRedisTemplate.opsForZSet().incrementScore(sortedSetName, key, x);
    }

    public Set<ZSetOperations.TypedTuple<String>> reverseRangeWithScores(String sortedSetName,Integer start, Integer end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(sortedSetName, start, end);
    }

    public void removeZset(String sortedSetName,String value) {
        stringRedisTemplate.opsForZSet().remove(sortedSetName, value);
    }

}
