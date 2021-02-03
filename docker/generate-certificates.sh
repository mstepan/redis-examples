#!/usr/bin/env bash
mkdir -p certs
openssl genrsa -out certs/ca.key 4096
openssl req \
    -x509 -new -nodes -sha256 \
    -key certs/ca.key \
    -days 3650 \
    -subj '/O=Oracle/CN=Certificate Authority' \
    -out certs/ca.crt
openssl genrsa -out certs/redis.key 2048
openssl req \
    -new -sha256 \
    -key certs/redis.key \
    -subj '/O=Oracle/CN=Server' | \
    openssl x509 \
        -req -sha256 \
        -CA certs/ca.crt \
        -CAkey certs/ca.key \
        -CAserial certs/ca.txt \
        -CAcreateserial \
        -days 365 \
        -out certs/redis.crt
openssl dhparam -out certs/redis.dh 2048
