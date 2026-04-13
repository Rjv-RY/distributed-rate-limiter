package com.rjv.distributed_sys.ratelimiter.core;

//
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;

import com.rjv.distributed_sys.ratelimiter.config.RateLimiterFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

//@Component
//@Primary
//public class MeteredRateLimiter implements RateLimiter{
//	
//	private final RateLimiter delegate;
//	private final Counter allowedCounter;
//	private final Counter deniedCounter;
//	private final Timer latencyTimer;
//	
//	public MeteredRateLimiter(
//			RateLimiterFactory.RateLimiterDelegate delegate,
//			MeterRegistry meterRegistry) {
//		this.delegate = delegate.getRateLimiter();
//		
//		//create metrics
//		this.allowedCounter = Counter.builder("ratelimiter.requests.allowed")
//				.description("number of requests allowed")
//				.register(meterRegistry);
//		
//		this.deniedCounter = Counter.builder("ratelimiter.requests.denied")
//				.description("number of requests denied")
//				.register(meterRegistry);
//		
//		this.latencyTimer = Timer.builder("ratelimiter.latency")
//				.description("rate limiter check latency")
//				.register(meterRegistry);
//	}
//	
//	@Override
//	public boolean allowRequest(String userId) {
//		return latencyTimer.record(() -> {
//			boolean allowed = delegate.allowRequest(userId);
//			
//			if (allowed) {
//				allowedCounter.increment();
//			} else {
//				deniedCounter.increment();
//			}
//			
//			return allowed;
//		});
//	}
//}
