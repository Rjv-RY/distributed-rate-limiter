package com.rjv.distributed_sys.ratelimiter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;

public class TokenBucketTest {
	private TokenBucket rateLimiter;
	private RateLimitConfig config;
	
	@BeforeEach
	void setUp() {
		config = new RateLimitConfig();
		config.setRequestsPerWindow(10);
		config.setWindowSeconds(60);
		
		rateLimiter = new TokenBucket(config);
	}
	
	@Test
	void allowsInitialBurst() {
		//frest bucket, full tokens
		String userId = "alek";
		
		//hits capacity
		for (int i = 0; i < 10; i++) {
			boolean allowed = rateLimiter.allowRequest(userId);
			assertTrue(allowed, "Request " + (i+1) + " should be allowed");
		}
		
		//11th blocked, no tokens
		assertFalse(rateLimiter.allowRequest(userId), "11th request should be blocked");
	}
	
	@Test
	void refillsTokensOverTime() throws InterruptedException {
		//user with exhausted tokens
		String userId = "bob";
		for(int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(userId);
		}
		assertFalse(rateLimiter.allowRequest(userId), "should be blocked");
		
		//wait for refill
		//10 tokens in 60 seconds, 1 token per 6 seconds
		//test with smaller window
		config.setRequestsPerWindow(5);
		config.setWindowSeconds(5);
		rateLimiter = new TokenBucket(config);
		
		for (int i = 0; i < 5; i++) {
			rateLimiter.allowRequest(userId);
		}
		
		Thread.sleep(1100);
		
		boolean allowed = rateLimiter.allowRequest(userId);
		assertTrue(allowed, "request should be allowed after window reset");
	}
	
	@Test
	void doesntExceedBucketCapacity() throws InterruptedException {
		//given capacity 5, we wait for some time
		config.setRequestsPerWindow(5);
		config.setWindowSeconds(5);
		rateLimiter = new TokenBucket(config);
		
		String userId = "rob";
		
		//wait 6 seconds
		Thread.sleep(6000);
		
		//can't make more than 5 requets
		for (int i = 0; i < 5; i++) {
			assertTrue(rateLimiter.allowRequest(userId), "Request " + (i+1) + " should be allowed");
		}
		
		assertFalse(rateLimiter.allowRequest(userId), "6th request should be blocked");
	}
	
	@Test
	void maintainsSeparateBucketsPerUser() {
		//alek exhausts tokens again
        for (int i = 0; i < 10; i++) {
            rateLimiter.allowRequest("alek");
        }
        
		//bob requests successfully
		boolean bobAllowed = rateLimiter.allowRequest("bob");
		assertTrue(bobAllowed, "Bob has own bucket");
		
		//alek still blocked
		assertFalse(rateLimiter.allowRequest("alek"), "Alek still blocked");
	}
	
	@Test
	void handlesConcurrentRequets() throws InterruptedException{
		//mult threads make requests
		String userId = "dav";
		int threadCount = 5;
		int requestsPerThread = 3;
		
		Thread[] threads = new Thread[threadCount];
		int[] successCount = {0};
		
		//mult thread consume token
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(() -> {
				for (int j = 0; j < requestsPerThread; j++) {
					if (rateLimiter.allowRequest(userId)) {
						synchronized (successCount) {
							successCount[0]++;
						}
					}
				}
			});
			threads[i].start();
		}
		
		//wait for threads
		for(Thread thread : threads) {
			thread.join();
		}
		
		assertTrue(successCount[0] <= 10, "Doesn't allow more than capacity, got: " + successCount[0]);
		assertTrue(successCount[0] >= 10, "Allows up to capacity, got: " + successCount[0]);
		
	}
}
