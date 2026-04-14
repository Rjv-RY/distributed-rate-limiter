package com.rjv.distributed_sys.ratelimiter.core;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;
import com.rjv.distributed_sys.ratelimiter.storage.CounterStorage;

@Component("fixedWindowCounter")
public class FixedWindowCounter implements RateLimiter{
	private final CounterStorage storage;
	private final RateLimitConfig config;
	
	//inject redis storage
	//or in memory storage as fallback
	public FixedWindowCounter(CounterStorage storage, RateLimitConfig config) {
		this.storage = storage;
		this.config = config;
	}
	
	@Override
	public boolean allowRequest(String userId) {
		String key = "user:" + userId; 
		boolean result = storage.allowRequest(
			key,
			config.getRequestsPerWindow(),
			config.getWindowSeconds()
		);
		return result;
	}
}
