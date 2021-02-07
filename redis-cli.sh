#!/usr/bin/env bash

docker run \
  --network "my-redis-network" \
  --volume "$PWD/docker/certs:/certs" \
  --volume "$PWD/docker/certs-client:/certs-client" \
  -it redis:6.0 \
  redis-cli \
    -h redis-master \
    -p 6379 \
    --tls \
    --cert "/certs-client/client.pem" \
    --key "/certs-client/client.key" \
    --cacert "/certs/ca.pem"
