#!/usr/bin/env bash

# link https://codeburst.io/mutual-tls-authentication-mtls-de-mystified-11fa2a52e9cf

export CERTS_FOLDER=certs

rm -rf $CERTS_FOLDER
mkdir -p $CERTS_FOLDER

#------------------------------------------------
# Root CA
#------------------------------------------------

# Create self-signed certificate as CA
openssl req \
  -new \
  -x509 \
  -nodes \
  -days 3650 \
  -subj '/CN=my-ca' \
  -keyout $CERTS_FOLDER/ca.key \
  -out $CERTS_FOLDER/ca.pem

#------------------------------------------------
# Redis server certificate
#------------------------------------------------

# Generate RSA private key for redis
openssl genrsa \
  -out $CERTS_FOLDER/redis.key 2048

# Create certificate signed request (CSR)
openssl req \
  -new \
  -key $CERTS_FOLDER/redis.key \
  -subj '/CN=redis-server' \
  -out $CERTS_FOLDER/redis.csr

# Create signed by CA certificate from CSR for redis
openssl x509 \
  -req \
  -in $CERTS_FOLDER/redis.csr \
  -CA $CERTS_FOLDER/ca.pem \
  -CAkey $CERTS_FOLDER/ca.key \
  -CAcreateserial \
  -days 365 \
  -out $CERTS_FOLDER/redis.pem

# Generate Diffie-Hellman
openssl dhparam -out $CERTS_FOLDER/redis.dh 2048

#------------------------------------------------
# Client certificate
#------------------------------------------------
export CERTS_CLIENT_FOLDER=certs-client
rm -rf $CERTS_CLIENT_FOLDER
mkdir -p $CERTS_CLIENT_FOLDER

# RSA private key for client
openssl genrsa \
  -out $CERTS_CLIENT_FOLDER/client.key 2048

# Certificate signing request for client
openssl req \
  -new \
  -key $CERTS_CLIENT_FOLDER/client.key \
  -subj '/CN=my-client' \
  -out $CERTS_CLIENT_FOLDER/client.csr

# Signed by CA client certificate
openssl x509 \
  -req \
  -in $CERTS_CLIENT_FOLDER/client.csr \
  -CA $CERTS_FOLDER/ca.pem \
  -CAkey $CERTS_FOLDER/ca.key \
  -CAcreateserial \
  -days 365 \
  -out $CERTS_CLIENT_FOLDER/client.pem

