#!/usr/bin/env bash

docker-compose up -d --scale redis-slave=2 --scale redis-sentinel=3
