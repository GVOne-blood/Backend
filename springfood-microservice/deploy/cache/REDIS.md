# Redis Cache Deployment - Upstash

## Thông tin chung

**Platform:** Upstash (Serverless Redis)  
**Database Name:** springfood  
**Database ID:** 4c2f0a0f-27dc-42ff-8af2-f612f5ed8ae7  
**Tier:** Free Tier  
**Region:** AWS Singapore (ap-southeast-1)  
**Provider:** AWS

## Dashboard & Links

- **Console Dashboard:** https://console.upstash.com/redis/4c2f0a0f-27dc-42ff-8af2-f612f5ed8ae7?teamid=0
- **Documentation:** https://docs.upstash.com/redis

## Thông tin Server

### Endpoint Configuration
- **Endpoint:** adequate-walrus-72613.upstash.io
- **Port (TCP):** 6379
- **Port (REST):** 443 (HTTPS)
- **TLS/SSL:** Enabled (rediss://)
- **Token/Password:** gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM

### Connection Details

#### TCP Connection (Recommended for Spring Boot)
```
rediss://default:gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM@adequate-walrus-72613.upstash.io:6379
```

#### REST API Connection
```
URL: https://adequate-walrus-72613.upstash.io
Token: gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM
```

#### Redis CLI Command
```bash
redis-cli --tls -u redis://default:********@adequate-walrus-72613.upstash.io:6379
```

#### Environment Variables (.env)
```properties
# TCP Connection (for Spring Data Redis)
REDIS_URL=rediss://default:gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM@adequate-walrus-72613.upstash.io:6379
REDIS_HOST=adequate-walrus-72613.upstash.io
REDIS_PORT=6379
REDIS_PASSWORD=gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM

# REST API (for Upstash REST client)
UPSTASH_REDIS_REST_URL=https://adequate-walrus-72613.upstash.io
UPSTASH_REDIS_REST_TOKEN=gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM
```

## Resource Limits & Quotas

### Current Usage (as of March 15, 2026)

#### Commands
- **Used:** 0 / 10,000 per month
- **Current:** 0 commands executed

#### Bandwidth
- **Used:** 0 B / 50 GB
- **Limit:** 50 GB per month (Free tier)

#### Storage
- **Used:** 0 B / 256 MB
- **Limit:** 256 MB (Free tier)

#### Cost
- **Current:** $0.00
- **Plan:** Free Tier

### Free Tier Specifications
- **Max Commands:** 10,000 per month
- **Max Bandwidth:** 50 GB per month
- **Max Storage:** 256 MB
- **Max Concurrent Connections:** 1,000
- **Max Request Size:** 1 MB
- **Max Daily Bandwidth:** 1 GB

## Features & Capabilities

### Supported Protocols
- ✅ **TCP/TLS:** Native Redis protocol with SSL (rediss://)
- ✅ **REST API:** HTTP-based access for serverless environments
- ✅ **Read-Only Token:** Separate token for read-only access

### Redis Version
- **Version:** Redis 7.x compatible
- **Compatibility:** Supports most Redis commands

### Persistence
- **Type:** Durable storage
- **Backup:** Automatic snapshots
- **Replication:** Multi-region replication available (paid plans)

### Security
- **TLS/SSL:** Required for all connections
- **Authentication:** Token-based authentication
- **Network:** Public endpoint with TLS encryption
- **IP Whitelist:** Not available in Free tier

## Spring Boot Integration

### Dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>io.lettuce</groupId>
    <artifactId>lettuce-core</artifactId>
</dependency>
```

### Configuration (application.yml)
```yaml
spring:
  redis:
    url: ${REDIS_URL}
    ssl: true
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### Alternative Configuration (Host/Port/Password)
```yaml
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    ssl: true
    timeout: 2000ms
```

## Use Cases trong SpringFood

### 1. Session Management
- User sessions
- JWT token blacklist
- Shopping cart temporary storage

### 2. Caching
- Product catalog cache
- User profile cache
- API response cache
- Database query results

### 3. Real-time Features
- Chat typing indicators (chat service)
- Online user presence
- Real-time notifications queue

### 4. Rate Limiting
- API rate limiting
- Login attempt tracking
- Request throttling

### 5. Distributed Locks
- Order processing locks
- Inventory management
- Concurrent operation control

## Monitoring & Metrics

### Available Metrics
- Commands per second
- Bandwidth usage
- Storage usage
- Connection count
- Latency statistics
- Error rates

### Monitoring Tools
- **Upstash Console:** Real-time metrics dashboard
- **CLI:** `redis-cli` for manual inspection
- **Data Browser:** Web-based key browser (NEW feature)

## Performance Characteristics

### Latency
- **Region:** Singapore (ap-southeast-1)
- **Expected Latency:** 
  - From Singapore: < 5ms
  - From Southeast Asia: 10-30ms
  - From other regions: 50-200ms

### Throughput
- **Free Tier:** 10,000 commands/month
- **Upgrade for:** Higher throughput requirements

### Connection Pooling
- Recommended: Use connection pooling (Lettuce/Jedis)
- Max connections: 1,000 concurrent

## Backup & Recovery

### Automatic Backups
- Upstash automatically maintains data durability
- Point-in-time recovery not available in Free tier

### Manual Backup
- Use `SAVE` or `BGSAVE` commands
- Export via Data Browser
- Use `redis-cli --rdb` for RDB dumps

### Data Export
```bash
# Using redis-cli
redis-cli --tls -u redis://default:TOKEN@adequate-walrus-72613.upstash.io:6379 --rdb backup.rdb
```

## Migration Strategy

### From Local Redis to Upstash
1. Export data from local Redis: `redis-cli --rdb dump.rdb`
2. Import to Upstash: `redis-cli --tls -u UPSTASH_URL --pipe < dump.rdb`
3. Update application configuration
4. Test connection and functionality
5. Switch traffic to Upstash

### Key Naming Convention
Recommended prefix pattern:
```
springfood:{service}:{type}:{key}

Examples:
- springfood:chat:typing:user:123
- springfood:product:cache:product:456
- springfood:cart:session:abc123
- springfood:auth:token:blacklist:xyz789
```

## Troubleshooting

### Common Issues

#### 1. Connection Timeout
```
Error: Could not connect to Redis
```
**Solutions:**
- Verify SSL/TLS is enabled (`rediss://` not `redis://`)
- Check firewall/network settings
- Verify token/password is correct

#### 2. Authentication Failed
```
Error: NOAUTH Authentication required
```
**Solutions:**
- Ensure password is included in connection string
- Verify token hasn't been regenerated
- Check environment variables are loaded

#### 3. SSL/TLS Error
```
Error: SSL connection error
```
**Solutions:**
- Use `rediss://` protocol (with double 's')
- Ensure Lettuce or Jedis supports TLS
- Update Redis client library

#### 4. Command Not Supported
```
Error: ERR unknown command
```
**Solutions:**
- Check Upstash Redis compatibility
- Some commands may be restricted
- Use alternative commands or REST API

### Testing Connection

#### Using redis-cli
```bash
redis-cli --tls -u rediss://default:TOKEN@adequate-walrus-72613.upstash.io:6379 PING
```

#### Using curl (REST API)
```bash
curl https://adequate-walrus-72613.upstash.io/ping \
  -H "Authorization: Bearer gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM"
```

#### Using Spring Boot
```java
@Autowired
private RedisTemplate<String, String> redisTemplate;

public void testConnection() {
    redisTemplate.opsForValue().set("test:key", "test:value");
    String value = redisTemplate.opsForValue().get("test:key");
    log.info("Redis test: {}", value); // Should print "test:value"
}
```

## Cost Optimization

### Free Tier Limits
- Monitor command usage to stay within 10,000/month
- Use caching strategies to reduce Redis calls
- Implement TTL on all cached data
- Clean up unused keys regularly

### Upgrade Triggers
Consider upgrading when:
- Approaching 10,000 commands/month
- Need more than 256 MB storage
- Require multi-region replication
- Need advanced features (Kafka integration, etc.)

## Security Best Practices

### 1. Token Management
- Store tokens in environment variables
- Never commit tokens to Git
- Rotate tokens periodically
- Use read-only tokens where possible

### 2. Key Expiration
- Always set TTL on cached data
- Use `EXPIRE` or `SETEX` commands
- Prevent memory leaks from abandoned keys

### 3. Access Control
- Use separate tokens for different environments
- Implement application-level access control
- Monitor unusual access patterns

## Advanced Features

### REST API Usage
For serverless environments or HTTP-only access:

```javascript
// Example: Node.js/JavaScript
const response = await fetch('https://adequate-walrus-72613.upstash.io/set/mykey/myvalue', {
  headers: {
    'Authorization': 'Bearer gQAAAAAAARulAAIncDE5MjQxZTg2OTlmNWI0ZmIwOTdhYzRkMWM1ODg1Y2U3MXAxNzI2MTM'
  }
});
```

### Data Browser (NEW)
- Web-based interface to browse keys
- Search and filter capabilities
- Edit values directly
- Available in Upstash Console

### CLI Integration
```bash
# Set environment variable
export REDIS_URL="rediss://default:TOKEN@adequate-walrus-72613.upstash.io:6379"

# Use in scripts
redis-cli --tls -u $REDIS_URL SET mykey myvalue
```

## Next Steps

1. ✅ Redis đã được deploy và configured
2. ✅ Connection strings đã được cập nhật trong .env
3. ⏳ Test connection từ các services
4. ⏳ Implement caching strategy
5. ⏳ Setup monitoring và alerts
6. ⏳ Document key naming conventions
7. ⏳ Implement TTL policies
8. ⏳ Load test với production-like traffic

---

**Last Updated:** March 15, 2026  
**Maintained by:** Development Team  
**Review Schedule:** Monthly  
**Support:** https://upstash.com/docs/redis
