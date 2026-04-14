package com.rjv.distributed_sys.ratelimiter.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

@Component
@Primary
public class ResilientStorage implements CounterStorage{
	
	private static final Logger log = LoggerFactory.getLogger(ResilientStorage.class);
	
	private final RedisStorage redisStorage;
	private final InMemoryStorage fallbackStorage;
	
	private volatile boolean redisAvailable = true;
	private volatile long lastRedisCheckTime = 0;
	private static final long REDIS_RETRY_INTERVAL_MS = 5000;
	
	public ResilientStorage(RedisStorage redisStorage, InMemoryStorage fallbackStorage) {
		this.redisStorage = redisStorage;
		this.fallbackStorage = fallbackStorage;
	}
	
	@Override
	public boolean allowRequest(String key, int limit, int windowSeconds) {
		
		if (shouldTryRedis()) {
			
			try {
				boolean result = redisStorage.allowRequest(key, limit, windowSeconds);
				
				if(!redisAvailable) {
					log.info("redis connection restored");
					redisAvailable = true;
				}
				return result;
			} catch (RedisConnectionFailureException e) {
				handleRedisFailure(e);
				
			} catch (org.springframework.dao.QueryTimeoutException e) {
				handleRedisFailure(e);
			} catch (Exception e) {
				log.error("unexpected redis error for key: {}", key, e);
				handleRedisFailure(e);
			}
		}		
		boolean result = fallbackStorage.allowRequest(key, limit, windowSeconds);
		return result;
	}
	
	private boolean shouldTryRedis() {
		if (redisAvailable) {
			return true;
		}
		
		long now = System.currentTimeMillis();
		if (now - lastRedisCheckTime > REDIS_RETRY_INTERVAL_MS) {
			lastRedisCheckTime = now;
			log.info("Retrying redis connection");
			return true;
		}
		
		return false;
	}
	
    private void handleRedisFailure(Exception e) {
        if (redisAvailable) {
            log.error("Redis connection failed - falling back to in-memory storage", e);
            log.error("Error details: {}", e.getMessage());
            redisAvailable = false;
            lastRedisCheckTime = System.currentTimeMillis();
        }
    }
}
