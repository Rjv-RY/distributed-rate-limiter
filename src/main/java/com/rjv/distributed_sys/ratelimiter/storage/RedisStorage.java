package com.rjv.distributed_sys.ratelimiter.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Component
//@Primary
public class RedisStorage implements CounterStorage {
    
	private static final Logger log = LoggerFactory.getLogger(RedisStorage.class);
    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisStorage(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    //increment hits o race conditions, is atomic
    //expire auto cleans old counters
    //dual-level debugging logs hahahahahaha
    @Override
    public boolean allowRequest(String key, int limit, int windowSeconds) {
    	
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
        } catch (Exception e) {
            throw new RedisConnectionFailureException("Redis connection test failed", e);
        }
    	
        long currentTime = System.currentTimeMillis();
        long windowStart = (currentTime / (windowSeconds * 1000)) * (windowSeconds * 1000);
        String windowKey = key + ":" + windowStart;
        
        try {
        	
        	Long count = redisTemplate.opsForValue().increment(windowKey);
        	
            if (count == null) {
                return false;
            }
            
            if (count == 1) {
                redisTemplate.expire(windowKey, windowSeconds, TimeUnit.SECONDS);
            }
            
            boolean allowed = count <= limit;
            
            return allowed;
        	
        } catch (Exception e) {
        	return false;
        }        
    }
}