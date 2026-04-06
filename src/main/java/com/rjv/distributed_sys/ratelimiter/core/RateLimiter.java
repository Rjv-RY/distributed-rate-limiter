package com.rjv.distributed_sys.ratelimiter.core;

public interface RateLimiter {
	boolean allowRequest(String userId);
}
