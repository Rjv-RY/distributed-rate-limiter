package com.rjv.distributed_sys.ratelimiter.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;

@Component("tokenBucket")
public class TokenBucket implements RateLimiter{
	private final RateLimitConfig config;
	private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
	
	public TokenBucket(RateLimitConfig config) {
		this.config = config;
	}
	
	@Override
	public boolean allowRequest(String userId) {
		Bucket bucket = buckets.computeIfAbsent(userId,
				k -> new Bucket(config.getRequestsPerWindow()));
		
		return bucket.tryConsume();
	}
	
	private class Bucket{
		private final int capacity;
		private final double refillRate;
		private double tokens;
		private long lastRefillTime;
		
		Bucket(int capacity){
			this.capacity = capacity;
			this.tokens = capacity;
			this.lastRefillTime = System.currentTimeMillis();
			this.refillRate = (double) capacity / (config.getWindowSeconds() * 1000);
		}
		
		synchronized boolean tryConsume() {
			refill();
			
			if (tokens >= 1) {
				tokens -= 1;
				return true;
			}
			
			return false;
		}
		
		private void refill() {
			long now = System.currentTimeMillis();
			long timePassed = now - lastRefillTime;
			
			double tokensToAdd = timePassed * refillRate;
			tokens = Math.min(capacity, tokens + tokensToAdd);
			
			lastRefillTime = now;
		}
	}
}
