#!/usr/bin/env bash

docker exec -it docker_redis-master_1 redis-cli --tls \
  --cert "/certs/ca.pem" \
  --key "/certs/ca.key" \
  --cacert "/certs/ca.pem"
