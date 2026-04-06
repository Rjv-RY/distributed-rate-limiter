package com.rjv.distributed_sys.ratelimiter.storage;

public interface CounterStorage {
	boolean allowRequest(String key, int limit, int windowSeconds);
}
