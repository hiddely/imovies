# InfSec project server code
2016

## Setup instructions
Copy `application.properties.example` file to `application.properties` and set appropriate database settings

Run `mvn spring-boot:run`

## SSL CA generation commands for KeyStore
Generate CA root private key (ECDSA 256 bits)

`keytool -genkeypair -alias imovieskeystore -keypass imovies -keystore imovieskeystore.pfx -storepass imovies -validity 10000 -keyalg EC -keysize 256 -storetype pkcs12`

