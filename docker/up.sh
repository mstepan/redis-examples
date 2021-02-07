#!/usr/bin/env bash

docker network create my-redis-network
docker-compose up -d --scale redis-slave=2 --scale redis-sentinel=3
