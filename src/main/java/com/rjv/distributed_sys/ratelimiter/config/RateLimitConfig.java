package com.rjv.distributed_sys.ratelimiter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import com.rjv.distributed_sys.ratelimiter.core.Algorithms;

@Configuration
@ConfigurationProperties(prefix = "ratelimiter.default")
public class RateLimitConfig {
	
	private int requestsPerWindow = 10;
	private int windowSeconds = 60;
	private Algorithms algorithm = Algorithms.FIXED_WINDOW;
	
	public int getRequestsPerWindow() {
		return requestsPerWindow;
	}
	
	public void setRequestsPerWindow(int requestsPerWindow) {
		this.requestsPerWindow = requestsPerWindow;
	}
	
	public int getWindowSeconds() {
		return windowSeconds;
	}
	
	public void setWindowSeconds(int windowSeconds) {
		this.windowSeconds = windowSeconds;
	}
	
	public Algorithms getAlgorithm() {
		return algorithm;
	}
	
	public void setAlgorithm(Algorithms alogrithm) {
		this.algorithm = alogrithm;
	}
}
