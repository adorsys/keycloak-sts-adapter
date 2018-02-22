# Keycloak STS Adapter

To run this multibanking mock on your local machine, you need a keycloak server configuered to add user encryption secret to the access token.

## Setting up Keycloak

```
> git clone https://github.com/adorsys/keycloak-sts-adapter.git

```
Check instructions from the readme-new.md file. 

## Running

This still runs on the branch sts-upgrade

```
> git clone https://git.adorsys.de/adorsys/multibanking-mock.git

> git checkout sts-upgrade

> mvn spring-boot:run

```
- Keycloak runs on ports: 8080 / 8787 (debug)
- Multibanking mock runs on ports: 10010 / 10017 (debug)
- Multibanking service if started runs on ports: 8081 / 10022 (debug)

