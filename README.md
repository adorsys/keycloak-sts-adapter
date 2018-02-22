# Keycloak STS Adapter

Set of adapters for the extension of keycloak for use with the adorsys security token service.

```
> git clone https://github.com/adorsys/keycloak-sts-adapter.git

```
## External STS

The module /keycloak-sts-adapter/keycloak-storage-provider is configured to:
- Install a storage Provider that forwards login request to STS
- Install a protocol mapper that inserts the resulting user secret into the user's access token.


## Embedded STS

The module /keycloak-sts-adapter/user-secret-adapter-embedded is a combination of following modules:

### /keycloak-sts-adapter/user-secret-auth-provider

This modules installs tow authentication flows:
- "sts browser" as a replacement for the native "browser" flow
- "sts direct" grant as a replace ment for the native "direct grant" flow

Both module have the purpose of intercepting the login process of the user and recover information need to add user secret to the token. These are:
- Password: user password is needed to recover a "user-main-secret". 
- scope: scope parameter added to the authentication request is used to find out the intended audience. These are the servers that need a secret to encrypt/decrypt user data.
 
For each audience, the user-main-secret is used to encrypt all "user-data-secret" before storing it to the database and to decrypt it before insertion in the access token. 

Both module then forward the authentication to the keycloak subsystem.

### /keycloak-sts-adapter/user-secret-protocol-mapper

This module encrypts and insert the user secret into the user's access token for the give audiences. Note that in order to add a secret to the token, the authorization request will have to add a scope paramater containing the name of the intended audience space separated. Below is a sample auth request:
```
> curl -v -X POST http://localhost:8080/auth/realms/multibanking/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d username=testuser -d password=testpassword -d grant_type=password -d client_id=multibanking-client -d 'scope=multibanking mockbanking'

```

### Running Embedded Version

Install jq for json parsing: https://stedolan.github.io/jq/download/

Download the keycloak distribution and set the corresponding env variable.

https://downloads.jboss.org/keycloak/3.4.3.Final/keycloak-3.4.3.Final.tar.gz

```
export KEYCLOAK_DIST=~/Downloads/keycloak-3.4.3.Final.tar.gz

> cd user-secret-adapter-embedded

> ./start-local.sh

let keycloak start

In a new shell

> ./init-local.sh 
 
Ends with something like
MB-CLIENT-ID 1d855741-7f31-413f-b9f0-d3dc09c8e516

```
### Restart Without losing config

```
> ./restart-local.sh

```

### Testing Embedded Version

- Start the multibanking service
- Start the multibanking mock

Use the following command to generate a token:

```
> curl -v -X POST http://localhost:8080/auth/realms/multibanking/protocol/openid-connect/token -H 'Content-Type: application/x-www-form-urlencoded' -d username=testuser -d password=testpassword -d grant_type=password -d client_id=multibanking-client -d 'scope=multibanking mockbanking'

```


