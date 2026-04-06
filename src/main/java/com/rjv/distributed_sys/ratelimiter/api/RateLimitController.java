package com.rjv.distributed_sys.ratelimiter.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rjv.distributed_sys.ratelimiter.core.RateLimiter;

@RestController
@RequestMapping("/api")
public class RateLimitController {
	private final RateLimiter rateLimiter;
	
	public RateLimitController(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}
	
//    public RateLimitController(@Qualifier("tokenBucket") RateLimiter rateLimiter) {
//        this.rateLimiter = rateLimiter;
//    }
	
	@GetMapping("/resource")
	public ResponseEntity<String> getResource(@RequestHeader("X-User-ID") String userId){
		if (rateLimiter.allowRequest(userId)) {
			return ResponseEntity.ok("Request allowed.");
		}
		
		return ResponseEntity.status(429).body("Rate limit exceeded. Try again later.");
	}
}
