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
		
		//debug
		log.info("ResilientStorage in use, using redis and inmemory fallabck");
		log.info("   - RedisStorage: {}", redisStorage.getClass().getName());
        log.info("   - FallbackStorage: {}", fallbackStorage.getClass().getName());
	}
	
	@Override
	public boolean allowRequest(String key, int limit, int windowSeconds) {
		//debug
        System.out.println("ResilientStorage.allowRequest called");
        System.out.println("Key: " + key + ", Limit: " + limit);
        System.out.println("redis available: " + redisAvailable);
		
		if (shouldTryRedis()) {
			//debug
			System.out.println("Trying Redis...");
			System.out.println("About to call redisStorage.allowRequest()...");
			
			try {
				boolean result = redisStorage.allowRequest(key, limit, windowSeconds);
				
				//debug
			    System.out.println("Redis call COMPLETED without exception");
			    System.out.println("Result: " + result);
				
				if(!redisAvailable) {
					log.info("redis connection restored");
					redisAvailable = true;
				}
				//debug
				System.out.println(" redis success status result: " + result);
				return result;
			} catch (RedisConnectionFailureException e) {
				//debug
				System.out.println("    Redis failed (connection): " + e.getMessage());
				handleRedisFailure(e);
				
			} catch (org.springframework.dao.QueryTimeoutException e) {
				//debug
				System.out.println("    Redis failed (timeout): " + e.getMessage());
				handleRedisFailure(e);
			} catch (Exception e) {
				//debug
				System.out.println("    Redis failed (unexpected): " + e.getMessage());
				log.error("unexpected redis error for key: {}", key, e);
				handleRedisFailure(e);
			}
		}
		
		//debug
		System.out.println("Using fallback InMemoryStorage");
		boolean result = fallbackStorage.allowRequest(key, limit, windowSeconds);
		//debug
		System.out.println("Fallback result: " + result);
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
