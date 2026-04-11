package com.rjv.distributed_sys.ratelimiter.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;
import com.rjv.distributed_sys.ratelimiter.core.FixedWindowCounter;
import com.rjv.distributed_sys.ratelimiter.core.RateLimiter;
import com.rjv.distributed_sys.ratelimiter.core.SlidingWindowLog;
import com.rjv.distributed_sys.ratelimiter.core.TokenBucket;
import com.rjv.distributed_sys.ratelimiter.storage.RedisStorage;

@SpringBootTest
public class AlgorithmBenchmark {
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	
	@Autowired
	private RateLimitConfig config;
	
	private FixedWindowCounter fixedWindow;
	private TokenBucket bucket;
	private SlidingWindowLog slidingWindow;
	
	@BeforeEach
	void setup() {
		redisTemplate.getConnectionFactory()
					.getConnection()
					.serverCommands()
					.flushAll();
		
		RedisStorage storage = new RedisStorage(redisTemplate);
		fixedWindow = new FixedWindowCounter(storage, config);
		bucket = new TokenBucket(config);
		slidingWindow = new SlidingWindowLog(redisTemplate, config);
	}
	
	@Test
	void fixedWindowTest() throws InterruptedException {
		System.out.println("\n=== fixed window benchmark:-");
		runBenchmark(fixedWindow, "FixedWindow");
	}
	
	@Test 
	void tokenBucketTest() throws InterruptedException {
		System.out.println("\n=== token bucket benchmark:-");
		runBenchmark(bucket, "TokenBucket");
	}
	
	@Test
	void slidingWindowTest() throws InterruptedException {
		System.out.println("\n=== sliding window benchmark:-");
		runBenchmark(slidingWindow, "SlidingWindow");
	}
	
	public void runBenchmark(RateLimiter limiter, String name) throws InterruptedException {
		int totalRequests = 1000;
		int threads = 10;
		String userId = "benchmarker";
		
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		CountDownLatch latch = new CountDownLatch(totalRequests);
		
		AtomicInteger allowed = new AtomicInteger(0);
		AtomicInteger denied = new AtomicInteger(0);
		
		long startTime = System.currentTimeMillis();
		
		for (int i = 0; i < totalRequests; i++) {
			executor.submit(() -> {
				try {
					if (limiter.allowRequest(userId)) {
						allowed.incrementAndGet();
					} else {
						denied.incrementAndGet();
					}
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		long endTime = System.currentTimeMillis();
		
		executor.shutdown();

        long duration = endTime - startTime;
        double throughput = (totalRequests * 1000.0) / duration;
        
        System.out.println("algorithm: " + name);
        System.out.println("total requests: " + totalRequests);
        System.out.println("allowed: " + allowed.get());
        System.out.println("denied: " + denied.get());
        System.out.println("duration: " + duration + "ms");
        System.out.println("throughput: " + String.format("%.2f", throughput) + " req/s");
        System.out.println();
	}
}
