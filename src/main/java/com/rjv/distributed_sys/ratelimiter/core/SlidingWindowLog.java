package com.rjv.distributed_sys.ratelimiter.core;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;

@Component("slidingWindowLog")
public class SlidingWindowLog implements RateLimiter{
	
	private final RedisTemplate<String, String> redisTemplate;
	private final RateLimitConfig config;
	
	public SlidingWindowLog(RedisTemplate<String, String> redisTemplate, RateLimitConfig config) {
		this.redisTemplate = redisTemplate;
		this.config = config;
//		System.out.println("sliding window loaded");
	}
	
	@Override
	public boolean allowRequest(String userId) {
		String key = "swl:" + userId;
		long currentTime = System.currentTimeMillis();
		long windowStart = currentTime - (config.getWindowSeconds() * 1000L);
		
		redisTemplate.opsForZSet().removeRangeByScore(key,  0, windowStart);
		
		Long count = redisTemplate.opsForZSet().zCard(key);
		
		if (count != null && count < config.getRequestsPerWindow()) {
			String requestId = currentTime + ":" + System.nanoTime();
			redisTemplate.opsForZSet().add(key, requestId, currentTime);
			
			redisTemplate.expire(key, config.getWindowSeconds() + 1, TimeUnit.SECONDS);
			return true;
		}
		return false;
	}
}
