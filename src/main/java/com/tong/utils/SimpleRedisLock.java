package com.tong.utils;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock {

    private StringRedisTemplate stringRedisTemplate;
    private String name;

    public SimpleRedisLock(StringRedisTemplate stringRedisTemplate, String name) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.name = name;
    }

    private static final String KEY_PREFIX = "lock:";
    private static final String THREAD_ID_PREFIX = UUID.randomUUID() + "-";

    public boolean tryLock(long timeoutSec){
        String key = KEY_PREFIX + name;
        long threadId = Thread.currentThread().getId();
        String value = THREAD_ID_PREFIX + threadId;
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeoutSec, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);
    }

    public void unlock(){
        // 获取线程标识
        String threadId = THREAD_ID_PREFIX + Thread.currentThread().getId();
        // 获取锁中存储的标识
        String key = KEY_PREFIX + name;
        String redisThreadId = stringRedisTemplate.opsForValue().get(key);
        // 判断标识是否一致
        if(threadId.equals(redisThreadId)) {
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }
}
