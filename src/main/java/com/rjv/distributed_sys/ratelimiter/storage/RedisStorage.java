package com.rjv.distributed_sys.ratelimiter.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@Component
@Primary
public class RedisStorage implements CounterStorage {
    
	private static final Logger log = LoggerFactory.getLogger(RedisStorage.class);
    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisStorage(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
//        System.out.println("RedisStorage created with RedisTemplate: " + redisTemplate);
    }
    
    //increment hits o race conditions, is atomic
    //expire auto cleans old counters
    //dual-level debugging logs hahahahahaha
    @Override
    public boolean allowRequest(String key, int limit, int windowSeconds) {
//    	System.out.println("RedisStorage.allowRequest called - Key: " + key);
    	
        long currentTime = System.currentTimeMillis();
        long windowStart = (currentTime / (windowSeconds * 1000)) * (windowSeconds * 1000);
        String windowKey = key + ":" + windowStart;
        
//        log.debug("checking rate limit for key: {}", windowKey);
        
        try {
        	
        	Long count = redisTemplate.opsForValue().increment(windowKey);
        	
            if (count == null) {
//            	log.error("Redis increment returned null for key: {}", windowKey);
                return false;
            }
            
            if (count == 1) {
                redisTemplate.expire(windowKey, windowSeconds, TimeUnit.SECONDS);
//                log.debug("Set expiration for key: {} ({} seconds)", windowKey, windowSeconds);
            }
            
            boolean allowed = count <= limit;
//            log.debug("Key: {}, Count: {}, Limit: {}, Allowed: {}", windowKey, count, limit, allowed);
//            System.out.println("Redis check complete - Count: " + count + ", Allowed: " + allowed);
            
            return allowed;
        	
        } catch (Exception e) {
//        	log.error("Redis error for key: {}", windowKey, e);
//        	System.err.println("Redis error: " + e.getMessage());
        	return false;
        }        
    }
    
    @PostConstruct
    public void testConnection() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                                       .getConnection()
                                       .ping();
            System.out.println("Redis ping from app: " + pong);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}