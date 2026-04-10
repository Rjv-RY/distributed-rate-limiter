package com.rjv.distributed_sys.ratelimiter.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;
import com.rjv.distributed_sys.ratelimiter.storage.InMemoryStorage;

public class FixedWindowCounterTest {
	
	private FixedWindowCounter rateLimiter;
	private RateLimitConfig config;
	
	@BeforeEach
	void setUp() {
		config = new RateLimitConfig();
		config.setRequestsPerWindow(10);
		config.setWindowSeconds(60);
		
		InMemoryStorage storage = new InMemoryStorage();
		rateLimiter = new FixedWindowCounter(storage, config);
	}
	
	@Test
	void allowsRequestsUnderLimit() {
		//new user, 0 reqs
		String userId = "alek";
		
		boolean allowed = rateLimiter.allowRequest(userId);
		for (int i = 0; i < 10; i++) {
			assertTrue(allowed, "Request " + (i+1) + " should be allowed");
		}
	}
	
	@Test
	void blocksRequestsOverLimit() {
		//user with exhausted limits
		String userId = "bob";
		
		for (int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(userId);
		}
		//on 11th request, blocked
		assertFalse(rateLimiter.allowRequest(userId), "11th request should be blocked");
	}
	
	@Test
	void resetsCounterAfterWindowExpires() throws InterruptedException {
		//user with exhausted limits
		String userId = "rob";
		for(int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(userId);
		}
		assertFalse(rateLimiter.allowRequest(userId), "should be blocked");
		
		//wait for window to expire (test 2 sec window)
		config.setWindowSeconds(2);
		rateLimiter = new FixedWindowCounter(new InMemoryStorage(), config);
		
		for (int i = 0; i < 10; i++) {
			rateLimiter.allowRequest(userId);
		}
		
		//wait lil more than 2 secs
		Thread.sleep(2100);
		
		assertTrue(rateLimiter.allowRequest(userId), "request should be allowed after window reset");
	}
	
	@Test
	void maintainsSeparateCounterPerUser() {
		//user alek exhausts limit
		for (int i = 0; i < 10; i++) {
			rateLimiter.allowRequest("alek");
		}
		
		//bob requests
		assertTrue(rateLimiter.allowRequest("bob"), "Bob has own counter");
		
		//alek still blocked
		assertFalse(rateLimiter.allowRequest("alek"), "Alek still blocked");
	}
	
	@Test
	void handlesEmptyUserId() {
		//what happens when user ID empty
		boolean allowed = rateLimiter.allowRequest("");
		assertTrue(allowed, "Empty string should be treated as valid user");
	}
}
