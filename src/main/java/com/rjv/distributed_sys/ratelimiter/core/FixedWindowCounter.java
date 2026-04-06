package com.rjv.distributed_sys.ratelimiter.core;
//
//import org.springframework.stereotype.Component;
//
//import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;
//import com.rjv.distributed_sys.ratelimiter.storage.CounterStorage;
//
//@Component
//public class FixedWindowCounter implements RateLimiter{
//	private final CounterStorage storage;
//	private final RateLimitConfig config;
//	
//	public FixedWindowCounter(CounterStorage storage, RateLimitConfig config) {
//		this.storage = storage;
//		this.config = config;
//		
//	}
//	
//	@Override
//	public boolean allowRequest(String userId) {
//		return storage.allowRequest(
//			userId,
//			config.getRequestsPerWindow(),
//			config.getWindowSeconds()
//		);
//			
//	}
//}
