package com.rjv.distributed_sys.ratelimiter.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryStorage implements CounterStorage{
	private final Map<String, WindowData> storage = new ConcurrentHashMap<>();
	
	@Override
	public boolean allowRequest(String key, int limit, int windowSeconds) {
		long currentTime = System.currentTimeMillis();
		long windowStart = (currentTime/(windowSeconds * 1000)) * (windowSeconds * 1000);
		
		storage.compute(key, (k, existingData) -> {
			//if new window, reset counter
			if (existingData == null || existingData.windowStart != windowStart) {
				return new WindowData(windowStart, 1);
			}
			
			//if same window, increment counter
			existingData.count++;
			
			return existingData;
		});
		
		return storage.get(key).count <= limit;
	}
	
	private static class WindowData{
		long windowStart;
		int count;
		
		WindowData(long windowStart, int count){
			this.windowStart = windowStart;
			this.count = count;
		}
	}
}
