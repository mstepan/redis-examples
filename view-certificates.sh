#!/usr/bin/env bash

openssl x509 -in docker/certs/ca.pem -text -noout
openssl x509 -in docker/certs/redis.pem -text -noout
openssl x509 -in docker/certs-client/client.pem -text -noout
