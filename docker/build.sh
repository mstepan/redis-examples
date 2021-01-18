#!/usr/bin/env bash

# Create Redis master image
docker build -f redis/Dockerfile --build-arg REDIS_CONFIG="redis-master.conf" -t com.max.redis-master:6.0 redis || exit 1

# Create Redis slave image
docker build -f redis/Dockerfile --build-arg REDIS_CONFIG="redis-slave.conf" -t com.max.redis-slave:6.0 redis || exit 1

# Create sentinel image
docker build -f sentinel/Dockerfile -t com.max.redis-sentinel:6.0 sentinel || exit 1
