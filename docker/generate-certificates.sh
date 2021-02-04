#!/usr/bin/env bash
mkdir -p certs

# Generate RSA private key 'ca.key'
openssl genrsa -out certs/ca.key 4096

# Generate certificate with public key 'ca.crt'
openssl req \
    -x509 -new -nodes -sha256 \
    -key certs/ca.key \
    -days 3650 \
    -subj '/O=Oracle/CN=Certificate Authority' \
    -out certs/ca.crt

# Generate RSA private key 'redis.key'
openssl genrsa -out certs/redis.key 2048

# Generate 'redis.crt' certificate using 'ca.key' as CA signer key
# so we have the following certificates chain: redis --> ca
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

# Generate Diffie-Hellman
openssl dhparam -out certs/redis.dh 2048
