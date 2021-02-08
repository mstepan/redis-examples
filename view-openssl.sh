#!/usr/bin/env bash
openssl x509 -showcerts -in $1 -text -noout
