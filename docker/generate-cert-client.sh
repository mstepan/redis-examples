#!/usr/bin/env bash

#------------------------------------------------
# Client certificate
#------------------------------------------------
export CERTS_FOLDER=certs
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

# Signed by CA client certificate (ca -> client)
openssl x509 \
  -req \
  -in $CERTS_CLIENT_FOLDER/client.csr \
  -CA $CERTS_FOLDER/ca.pem \
  -CAkey $CERTS_FOLDER/ca.key \
  -CAcreateserial \
  -days 365 \
  -out $CERTS_CLIENT_FOLDER/client.pem

#------------------------------------------------
# JKS java part
#------------------------------------------------

rm -rf $CERTS_CLIENT_FOLDER/truststore.jks

# Add CA certificate to truststore
keytool -import -v -trustcacerts -alias ca -file $CERTS_FOLDER/redis.pem -keystore $CERTS_CLIENT_FOLDER/truststore.jks

