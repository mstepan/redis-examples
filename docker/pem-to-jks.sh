#!/usr/bin/env bash

# extract redis certificate from 'certs/redis.crt' and add to 'certs/truststore.jks'
# 'certs/truststore.jks' will be used to connect from java app using Jedis to Redis instance with TLS
keytool -import -v -trustcacerts -alias ca -file certs/redis.pem -keystore certs/truststore.jks
