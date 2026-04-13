package com.rjv.distributed_sys.ratelimiter.core;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;

@Component("slidingWindowLog")
public class SlidingWindowLog implements RateLimiter{
	
	private static final Logger log = LoggerFactory.getLogger(SlidingWindowLog.class);
	private final RedisTemplate<String, String> redisTemplate;
	private final RateLimitConfig config;
	
	private volatile boolean redisAvailable = true;
	
	public SlidingWindowLog(RedisTemplate<String, String> redisTemplate, RateLimitConfig config) {
		this.redisTemplate = redisTemplate;
		this.config = config;
//		System.out.println("sliding window loaded");
	}
	
	@Override
	public boolean allowRequest(String userId) {
		String key = "swl:" + userId;
		
		try {
			long currentTime = System.currentTimeMillis();
			long windowStart = currentTime - (config.getWindowSeconds() * 1000L);
			
			redisTemplate.opsForZSet().removeRangeByScore(key,  0, windowStart);
			
			Long count = redisTemplate.opsForZSet().zCard(key);
			
			if (count != null && count < config.getRequestsPerWindow()) {
				String requestId = currentTime + ":" + System.nanoTime();
				redisTemplate.opsForZSet().add(key, requestId, currentTime);
				
				redisTemplate.expire(key, config.getWindowSeconds() + 1, TimeUnit.SECONDS);
				if (!redisAvailable) {
					log.info("Redis connection stored (SlidingWindowLog)");
					redisAvailable = true;
				}
				return true;
			}
			return false;
			
		} catch(RedisConnectionFailureException e) {
			if (redisAvailable) {
				log.error("Redis connection failed in SlidingWindowLog, Allowing Request", e);
				redisAvailable = false;
			}
			return true;
		} catch(Exception e) {
			log.error("Unexpected redis error in SlidingWindowLog", e);
			return true;
		}
	}
}
