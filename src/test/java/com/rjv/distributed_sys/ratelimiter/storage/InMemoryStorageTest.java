package com.rjv.distributed_sys.ratelimiter.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InMemoryStorageTest {
	private InMemoryStorage storage;
	
	@BeforeEach
	void setUp() {
		storage = new InMemoryStorage();
	}
	
	@Test
	void allowaRequestsUnderLimit() {
		String key = "user:alek";
		int limit = 5;
		int windowSeconds = 60;
		
		//make reqs under limit
		for (int i = 0; i < 5; i++) {
			assertTrue(storage.allowRequest(key, limit, windowSeconds), "Request " + (i + 1) + " should be allowed");
		}
	}
	
	@Test
	void blocksRequestsOverLimit() {
		String key = "user:bob";
		int limit = 3;
		int windowSeconds = 60;
		
		//exhaust limit
		for (int i = 0; i < 3; i++) {
			storage.allowRequest(key, limit, windowSeconds);
		}
		
		assertFalse(storage.allowRequest(key, limit, windowSeconds), "requests over limit be blocked");
	}
	
	@Test
	void resetsCounterInNewWindow() throws InterruptedException {
		String key = "user:rob";
		int limit = 5;
		int windowSeconds = 2; //for faster test
		
		//exhaust limit
		for (int i = 0; i < 5; i++) {
			storage.allowRequest(key, limit, windowSeconds);
		}
		
		assertFalse(storage.allowRequest(key, limit, windowSeconds));
		
		//new window
		Thread.sleep(2100);
		
		//allows
		assertTrue(storage.allowRequest(key, limit, windowSeconds), "allows requests in new window");
	}
	
	@Test
	void handlesMultipleKeysIndependently() {
		int limit = 3;
		int windowSeconds = 60;
		
		for (int i = 0; i < 3; i++) {
			storage.allowRequest("user:alek", limit, windowSeconds);
		}
		
		assertTrue(storage.allowRequest("user:bob", limit, windowSeconds), "different keys have separate counters");
		
		assertFalse(storage.allowRequest("user:alek", limit, windowSeconds), "alek still blocked");
	}
}
