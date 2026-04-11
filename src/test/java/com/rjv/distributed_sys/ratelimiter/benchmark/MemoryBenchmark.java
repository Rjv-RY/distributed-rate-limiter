package com.rjv.distributed_sys.ratelimiter.benchmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class MemoryBenchmark {
	
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Test
	void compareMemoryUsage() {
		redisTemplate.getConnectionFactory()
					.getConnection()
					.serverCommands()
					.flushAll();
		
		System.out.println("\n=== memory usage comparison ===");
		
		//fixedWindow - one counter per user
		redisTemplate.opsForValue().set("user:alek:1712570000000", "10");
		long fixedWindowMemory = getMemoryUsed();
		System.out.println("fixed window (1 user, 1 window): ~" + fixedWindowMemory);
		
		redisTemplate.getConnectionFactory()
					.getConnection()
					.serverCommands()
					.flushAll();
		
		//slidingWindow
		for (int i = 0; i < 10; i++) {
			redisTemplate.opsForZSet().add(
			"swl:alek",
			"1712570000" + i + ":123456789",
			1212570000000L + i
			);
		}
		long slidingWindowMemory = getMemoryUsed();
		System.out.println("sliding window (1 user, 10 requests): ~" + slidingWindowMemory);
		
		System.out.println("\n memory overhead: " + String.format("%.1fx", (double)slidingWindowMemory / fixedWindowMemory));
	}
	
	private long getMemoryUsed() {
		String info = redisTemplate.getConnectionFactory()
					.getConnection()
					.serverCommands()
					.info("memory")
					.getProperty("used_memory");
		return Long.parseLong(info);
	}
}
