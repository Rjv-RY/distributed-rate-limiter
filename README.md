# Distributed Rate Limiter

A rate limiter written in Java 17 and SpringBoot 4 built on the principles I learned in DDIA. 

### WIP.

**Current Status:** Distributed Build: Persistent Storage for usage of Tokens across sessions/instances of the application using Redis. Three algorithms/approaches to test.

## Runing the app locally
```bash
./mvnw spring-boot:run
```

## Testing (Running using Docker)

Distributed Test:
- docker-compose down -v
- mvn clean package -DskipTests (skip tests)
- docker-compose up --build
- Test again on URLs 'http://localhost:8082/api/resource' and 'http://localhost:8081/api/resource'
- Header: X-User-ID: rob
- Make 10 successive requests (succeed), 11th fails with 429

## Configuration
Edit `application.properties`:
```properties
ratelimiter.default.requests-per-window=10
ratelimiter.default.window-seconds=60
ratelimiter.default.algorithm=SLIDING_WINDOW
spring.data.redis.host=localhost
spring.data.redis.port=6379 //or whatever port you wish
spring.data.redis.timeout=2000ms
ratelimiter.storage.type=redis
