#!/usr/bin/env bash

rm -rf certs/truststore.jks

cat certs-client/client.pem certs/ca.pem > certs-client/import.pem
openssl pkcs12 -export -in certs-client/import.pem -inkey certs-client/client.key -name client > certs-client/client.p12

keytool -importkeystore -srckeystore certs-client/client.p12
-destkeystore store.keys -srcstoretype pkcs12 -alias ca

# Import CA certificate
#keytool -import -v -trustcacerts -alias ca -file certs/ca.pem -keystore certs/truststore.jks

# View certificates
#keytool -list -v -keystore certs/truststore.jks

