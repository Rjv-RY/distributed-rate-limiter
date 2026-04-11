package com.rjv.distributed_sys.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.rjv.distributed_sys.ratelimiter.core.FixedWindowCounter;
import com.rjv.distributed_sys.ratelimiter.core.TokenBucket;
import com.rjv.distributed_sys.ratelimiter.core.SlidingWindowLog;

import com.rjv.distributed_sys.ratelimiter.core.Algorithms;
import com.rjv.distributed_sys.ratelimiter.core.RateLimiter;


@Configuration
public class RateLimiterFactory {
	
	private final RateLimitConfig config;
	private final FixedWindowCounter fixedWindowCounter;
	private final TokenBucket tokenBucket;
	private final SlidingWindowLog slidingWindowLog;
	
	public RateLimiterFactory(
			RateLimitConfig config,
			FixedWindowCounter fixedWindowCounter,
			TokenBucket tokenBucket,
			SlidingWindowLog slidingWindowLog) {
		this.config = config;
		this.fixedWindowCounter = fixedWindowCounter;
		this.tokenBucket = tokenBucket;
		this.slidingWindowLog = slidingWindowLog;
	}
	
	@Bean
	@Primary
	public RateLimiter rateLimiter() {
		Algorithms algorithm = config.getAlgorithm();
		
		System.out.println("ratelimiter algorithm: " + algorithm);
		
		switch (algorithm) {
		case FIXED_WINDOW:
			return fixedWindowCounter;
		case TOKEN_BUCKET:
			return tokenBucket;
		case SLIDING_WINDOW:
			return slidingWindowLog;
		default:
			throw new IllegalArgumentException("unknown algorithm: " + algorithm);
		}
	}
}
