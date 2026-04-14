package com.rjv.distributed_sys.ratelimiter.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rjv.distributed_sys.ratelimiter.config.RateLimitConfig;
import com.rjv.distributed_sys.ratelimiter.core.RateLimiter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@RestController
@RequestMapping("/api")
public class RateLimitController {
	
	private final RateLimiter rateLimiter;
    private final Counter allowedCounter;
    private final Counter deniedCounter;
    private final Timer latencyTimer;
    private final RateLimitConfig config;
	
	public RateLimitController(RateLimiter rateLimiter, MeterRegistry meterRegistry, RateLimitConfig config) {
		this.rateLimiter = rateLimiter;
		this.config = config;
		
        this.allowedCounter = Counter.builder("ratelimiter.requests.allowed")
                .description("Number of requests allowed")
                .register(meterRegistry);
        
        this.deniedCounter = Counter.builder("ratelimiter.requests.denied")
                .description("Number of requests denied")
                .register(meterRegistry);
        
        this.latencyTimer = Timer.builder("ratelimiter.latency")
                .description("Rate limiter check latency")
                .register(meterRegistry);
	}
	
	@GetMapping("/resource")
	public ResponseEntity<String> getResource(@RequestHeader("X-User-ID") String userId){
			
	    return latencyTimer.record(() -> {
	        boolean allowed = rateLimiter.allowRequest(userId);
	        
	        int limit = config.getRequestsPerWindow();
	        long resetTime = getWindowResetTime();
	        
	        if (allowed) {
	            allowedCounter.increment();
	            return ResponseEntity.ok()
	                    .header("X-RateLimit-Limit", String.valueOf(limit))
	                    .header("X-RateLimit-Reset", String.valueOf(resetTime))
	                    .body("Request allowed.");
	        } else {
	            deniedCounter.increment();
	            return ResponseEntity.status(429)
	                    .header("X-RateLimit-Limit", String.valueOf(limit))
	                    .header("X-RateLimit-Remaining", "0")
	                    .header("X-RateLimit-Reset", String.valueOf(resetTime))
	                    .header("Retry-After", String.valueOf(config.getWindowSeconds()))
	                    .body("Rate limit exceeded. Try again later.");
	        }
	    });
	}
	
	private long getWindowResetTime() {
		long windowSeconds = config.getWindowSeconds();
		long currentTime = System.currentTimeMillis();
		long windowStart = (currentTime/windowSeconds) * windowSeconds;
		return windowStart;
	}
}
