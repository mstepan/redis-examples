#!/usr/bin/env bash

docker exec -it  docker_redis-master_1 redis-cli --tls \
  --cert "/certs/redis.crt" \
  --key "/certs/redis.key" \
  --cacert "/certs/ca.crt"
