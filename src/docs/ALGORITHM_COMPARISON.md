# Algorithm Comparison

## Benchmark Results

> **Environment:** Local Machine, Redis 7-alpine, Java 17, 1000 concurrent requests

### Throughput (Req/Sec)

| --Algorithms-- | ---Throughput--- | -------Storage Backend------- | -----Notes----- |
|----------------|------------------|-------------------------------|-----------------|
| -Fixed Window- |   3703.70 req/s  |      Redis (network calls)    | Not Distributed |
| -Token Bucket- | 166,666.67 req/s | In-Memory (ConcurrentHashMap) |   Distributed   |
| Sliding Window |   2785.52 req/s  |      Redis Sorted Sets        |   Distributed   |

### In-Memory vs Redis Trade-Off

** Why Token Bucket is 60 times faster **

TokenBucket uses in-memory storage (no network), while Fixed and Sliding Window use Redis (network round trip per request)

**Trade-offs:**

- **Token Bucket(in-memory):**
    - Very Fast (166k reqs/sec)
    - Cannot coordinate across servers
    - Each server has a separate state
    - **Use Case:** Single-server use, local development.
    
- **Redis-based (Fixed/Sliding Window):**
    - Coordinates across multiple servers
    - Shared State
    - Slower due to network latency (3/4k reqs/s)
    - **Use Case:** Distributed system, production multi-server deployments
    
**Note:**
To get the best of both worlds (Speed and Distribution), you could write/implement a Token Bucket with Lua Scripts.

Why? Redis supports server-side scripting using Lua. Lua Script runs atomically on the redis server, ensuring all operations occur inside one block of the script and prevent race conditions. The script will handle calls from various app instances, read and refill tokens, set expiry etc as the single source of truth. This turns multiple network call round trips to one single round trip while being safer than ConcurrentHashMap while preventing race conditions.

### Memory Usage (Per User)

| Algorithm | Memory/User | Data Structure | Scalability |
|-----------|-------------|----------------|-------------|
| Fixed Window | ~100 bytes | Redis String (counter) | Scales horizontally |
| Token Bucket | ~200 bytes | In-memory bucket state | Per-server storage |
| Sliding Window | ~1,000 bytes | Redis Sorted Set (timestamps) | Scales horizontally |

### Accuracy

| Algorithms | Boundary behavior | Burst Allowed | Use-case |
|-----------|------------------|---------------|----------|
| Fixed Window | can allow 2x at window edge | user makes 10 req at 11:59:59, 10 more at 12:00:00, 20 in 2 secs | High-traffic public APIs |
| Token Bucket | smooth refill | Up to bucket capacity (configurable) | User-facing features (better UX) |
| Sliding Window | exact enforcement | No, strictly N requests per window | Paid APIs, security-critical endpoints |

**Node:**

Fixed Window has a boundary problem given it works by refreshing on time. So bursts could allow too many requests at once if the window just reset, leading to bypassed limits by the user. As such we use Sliding Window which doesn't have hard resets using times.

## Summary

**Best Algorithm always depends on your use-case and requirements:

 - Accuracy? Sliding Window
 - Smooth UX? Token Bucket
 - Distributed Coordination? Fixed/Sliding Window 
 - Speed AND Distribution? Token Bucket with Lua Script that runs on Redis
 