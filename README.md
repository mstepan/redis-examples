# Redis master slave replication setup with sentinels and mutual TLS

1. Redis master with mutual TLS 
2. Redis slaves with TLS (2 slaves in docker-compose)
3. Redis sentinel with TLS (3 sentinels in docker-compose)

4. Connect to Redis TLS instance from redis-cli (see `redis-cli.sh`)
5. Connect to Redis instance through sentinels using Java TLS 
(see `com.max.app.redis.TlsSentinelMain`)
