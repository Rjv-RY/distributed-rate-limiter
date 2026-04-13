package com.rjv.distributed_sys.ratelimiter.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryStorage implements CounterStorage{
	private final Map<String, WindowData> storage = new ConcurrentHashMap<>();
	
	@Override
	public boolean allowRequest(String key, int limit, int windowSeconds) {
		
		//debug
	    System.out.println("=== allowRequest called ===");
	    System.out.println("Key: " + key);
	    System.out.println("Limit: " + limit);
	    System.out.println("Window seconds: " + windowSeconds);
		
		long currentTime = System.currentTimeMillis();
		long windowStart = (currentTime/(windowSeconds * 1000)) * (windowSeconds * 1000);
		System.out.println("InMemoryStorage - Key: " + key + ", Current time: " + currentTime + ", Window start: " + windowStart);
		
		//debug
	    System.out.println("Current time: " + currentTime);
	    System.out.println("Calculated window start: " + windowStart);
	    System.out.println("Storage before compute: " + storage);
		
		WindowData data = storage.compute(key, (k, existingData) -> {
			//if new window, reset counter
			if (existingData == null || existingData.windowStart != windowStart) {
				System.out.println("new window, resetting counter to 1");
				return new WindowData(windowStart, 1);
			}
			
			//if same window, increment counter
			existingData.count++;
			System.out.println("same window, incrementing counter to: " + existingData.count);
			return existingData;
		});
		
		//debug
	    System.out.println("Data after compute - count: " + data.count + ", windowStart: " + data.windowStart);
	    System.out.println("Storage after compute: " + storage);
		
		boolean allowed = data.count <= limit;
		
		//debug
	    System.out.println("Final decision - Count: " + data.count + ", Limit: " + limit + ", Allowed: " + allowed);
	    System.out.println("=================================");
	    
		return allowed;
	}
	
	private static class WindowData{
		long windowStart;
		int count;
		
		WindowData(long windowStart, int count){
			this.windowStart = windowStart;
			this.count = count;
		}
		
	    @Override
	    public String toString() {
	        return "WindowData{windowStart=" + windowStart + ", count=" + count + "}";
	    }
	}
}
