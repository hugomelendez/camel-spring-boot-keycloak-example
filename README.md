# Camel, Spring Boot & Keycloak Example

Simple demonstration of a Camel REST route running in Spring Boot secured by Keycloak.

## Usage

### Keycloak

Deploy a Keycloak instance:

```bash
docker run \
    -p 9080:8080 \
    -e KEYCLOAK_USER=admin \
    -e KEYCLOAK_PASSWORD=admin \
    -e KEYCLOAK_IMPORT=/tmp/example-realm.json \
    -v $PWD/example-realm.json:/tmp/example-realm.json \
    quay.io/keycloak/keycloak:11.0.0
```

A realm called **example** will be imported with two clients created:

* **ui** (access_type: public)
* **camel** (access_type: bearer-only)

The **camel** client has the following client roles:

* **admin**
* **user**

The users with the assigned client roles to test the application are not included in the import (because of  a Keycloak limitation exporting the realm) so they need to be created manually and the client roles need to be mapped to the users.

In the demonstration two users are required:

* **John** (with the **user** role)
* **Dave** (with the **admin** role)

Once keycloak is configured, run the application:

```bash
mvn spring-boot:run
```

### Testing the services

There are two rest endpoints to test:

* **/camel/user/hello**
* **/camel/admin/hello**

First, test with user John, so a token for him is required:

```bash
JOHN_TOKEN=$(curl -sk --data "username=john&password=john&grant_type=password&client_id=ui" http://localhost:9080/auth/realms/example/protocol/openid-connect/token | jq -r .access_token)

curl -v -H "Authorization: Bearer $JOHN_TOKEN" http://localhost:8080/camel/user/hello
Hello User!
```

This is an allowed request, so it will be success.

If John tries to use the other endpoint, it will fail:

```bash
curl -v -H "Authorization: Bearer $JOHN_TOKEN"  http://localhost:8080/camel/admin/hello
{"timestamp":"2020-08-15T03:03:27.304+0000","status":403,"error":"Forbidden","message":"Forbidden","path":"/camel/admin/hello"}
```

On the contrary, Dave will be allowed to use both endpoints:

```bash
DAVE_TOKEN=$(curl -sk --data "username=dave&password=dave&grant_type=password&client_id=ui" http://localhost:9080/auth/realms/example/protocol/openid-connect/token | jq -r .access_token)

curl -v -H "Authorization: Bearer $DAVE_TOKEN"  http://localhost:8080/camel/user/hello
Hello User!

curl -v -H "Authorization: Bearer $DAVE_TOKEN"  http://localhost:8080/camel/admin/hello
Hello Admin!
```