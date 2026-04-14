# Distributed Rate Limiter

A rate limiter written in Java 17 and SpringBoot 4 built on the principles I learned in DDIA. 

### WIP.

**Current Status:** Distributed Build: Persistent Storage for usage of Tokens across sessions/instances of the application using Redis. Three algorithms/approaches to test.

## Testing (Distributed, and Degradation)
Test Prerequisites:
- Docker engine on desktop
- A unix-based shell (or git bash/Cygwin/wsl if on windows)

Test:
- cd into the app's root directory with a linux/bash-based shell
- bash demo.sh
- This will print the entire trace of the process in your console.

## Running Distributed Build:
In the app's root directory, run these commands:
- mvn clean package -DskipTests (skip tests)
- docker-compose up --build

This will run two instances of the application at:
1. http://localhost:8081/api/resource
2. http://localhost:8082/api/resource

Send requests to the endpoint using Postman or Curl, ensure that the header contains:
- X-User-ID: user

Any name or even an empty user works.

## Configuration
Edit `application.properties`:
```properties
ratelimiter.default.requests-per-window=10
ratelimiter.default.window-seconds=60
# can switch to FIXED_WINDOW or TOKEN_BUCKET, Token Bucket is in-memory, Fixed Window supports graceful degradation.
# check src/docks/ALGORITHM_COMPARISON.md for more about those
ratelimiter.default.algorithm=SLIDING_WINDOW 
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=2000ms
ratelimiter.storage.type=redis
