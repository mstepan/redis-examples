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
# https://stackoverflow.com/questions/2685512/can-a-java-key-store-import-a-key-pair-generated-by-openssl
#------------------------------------------------

rm -rf $CERTS_CLIENT_FOLDER/keystore.jks

# Convert to pkcs12 format
openssl pkcs12 -export \
  -in $CERTS_CLIENT_FOLDER/client.pem \
  -inkey $CERTS_CLIENT_FOLDER/client.key > $CERTS_CLIENT_FOLDER/client.p12

# Import client certificate to JKS keystore
keytool -importkeystore -srckeystore $CERTS_CLIENT_FOLDER/client.p12 \
  -destkeystore $CERTS_CLIENT_FOLDER/keystore.jks \
  -srcstoretype pkcs12

# Add redis certificate to JKS truststore
keytool -import -v -trustcacerts -alias ca -file $CERTS_FOLDER/redis.pem -keystore $CERTS_CLIENT_FOLDER/truststore.jks

# List JSK keystore entries
#keytool -list -v -keystore $CERTS_CLIENT_FOLDER/keystore.jks

# List JSK truststore entries
#keytool -list -v -keystore $CERTS_CLIENT_FOLDER/truststore.jks
