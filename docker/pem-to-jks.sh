#!/usr/bin/env bash
keytool -import -v -trustcacerts -alias ca -file certs/redis.crt -keystore certs/truststore.jks
