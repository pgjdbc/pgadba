#!/bin/sh

openssl genrsa -out rootCA.key 4096

# use 100 years, in order to make our unit tests work longer than I live
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 36500 -out rootCA.crt  -subj '/CN=localhost'

openssl genrsa -out localhost.key 2048

openssl req -new -key localhost.key -out localhost.csr -subj '/CN=localhost'

openssl x509 -req -in localhost.csr -text -days 36500 -CA rootCA.crt -CAkey rootCA.key -CAcreateserial -out localhost.crt
