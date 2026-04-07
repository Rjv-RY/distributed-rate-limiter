# Distributed Rate Limiter

A rate limiter written in Java 17 and SpringBoot 4 built on the principles I learned in DDIA. 

### WIP.

**Current Status:** Single-server implementation with two algorithms.

## Runing the app
```bash
./mvnw spring-boot:run
```

## Testing (using Postman)
Use Postman:
- URL: `http://localhost:8080/api/resource`
- Header: `X-User-ID: rob`
- Make 10 successive requests (succeed), 11th fails with 429
- Logs Token refill progress in console.

## Configuration
Edit `application.properties`:
```properties
ratelimiter.default.requests-per-window=10
ratelimiter.default.window-seconds=60
