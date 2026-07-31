# MySQL Data Source Setup

Step-by-step for creating a MySQL user/database and wiring it into a Spring Boot
service's `application.properties`, using the actual databases in this project
(`productservice` for `ProductService`, `userauthservice` for the upcoming
Auth/User Service — see `AuthenticationAndAuthorization.md`).

## Contents

1. [Prerequisites](#1-prerequisites)
2. [Case A — Database Already Exists (`productservice`)](#2-case-a--database-already-exists-productservice)
3. [Case B — New Database (`userauthservice`)](#3-case-b--new-database-userauthservice)
4. [Configure `application.properties`](#4-configure-applicationproperties)
5. [Verify the Connection](#5-verify-the-connection)
6. [Notes on the Passwordless User](#6-notes-on-the-passwordless-user)

---

## 1. Prerequisites

- MySQL Workbench connected to the target MySQL instance as a user with admin
  rights (typically `root`).
- Open a new SQL tab (Query1) against that connection — the commands below run
  there, not against a specific schema.

---

## 2. Case A — Database Already Exists (`productservice`)

Used for `ProductService`. The database already exists, so only the user and grant
are needed:

```sql
CREATE USER 'robinkumarqpcai'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON productservice.* TO 'robinkumarqpcai'@'localhost';
FLUSH PRIVILEGES;
```

If the user already exists (e.g. it was created for another service on the same
instance), skip `CREATE USER` and just run the `GRANT` + `FLUSH PRIVILEGES`.

---

## 3. Case B — New Database (`userauthservice`)

Used for the Auth/User Service. Create the schema first, then the user/grant:

```sql
CREATE DATABASE userauthservice;
CREATE USER 'robinkumarqpcai'@'localhost' IDENTIFIED BY '';
GRANT ALL PRIVILEGES ON userauthservice.* TO 'robinkumarqpcai'@'localhost';
FLUSH PRIVILEGES;
```

If `robinkumarqpcai@localhost` was already created in Case A, drop the
`CREATE USER` line — `CREATE USER` errors if the user already exists — and just
grant the new database to the existing user:

```sql
CREATE DATABASE userauthservice;
GRANT ALL PRIVILEGES ON userauthservice.* TO 'robinkumarqpcai'@'localhost';
FLUSH PRIVILEGES;
```

---

## 4. Configure `application.properties`

`ProductService`'s existing config
(`ProductService/src/main/resources/application.properties`) is the template:

```properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/productservice
spring.datasource.username=robinkumarqpcai
#spring.datasource.password=secret
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

For a new service, point the URL at its own database and leave the password line
commented (or blank) to match the passwordless user created above:

```properties
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/userauthservice
spring.datasource.username=robinkumarqpcai
#spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Only the database name in the URL changes between services — same host, same user.

---

## 5. Verify the Connection

1. In MySQL Workbench, open a new connection using `robinkumarqpcai` / (blank
   password) against `localhost:3306`, and confirm the target schema
   (`productservice` or `userauthservice`) is visible and writable.
2. Start the Spring Boot service. With `spring.jpa.hibernate.ddl-auto=create` (see
   `ProductService`'s config), a successful boot will create/recreate the schema's
   tables — check the console log for `HHH000232`/DDL output and no
   `Access denied for user` errors.

---

## 6. Notes on the Passwordless User

`IDENTIFIED BY ''` creates a user with no password, matching the commented-out
`spring.datasource.password` line in `application.properties`. This is acceptable
for local development against `localhost` only. It should **not** be used for any
database reachable from outside the local machine (Docker networks, staging,
production) — set a real password and populate
`spring.datasource.password` (ideally via an environment variable, the same way
`MYSQL_HOST` is externalized in the URL) before deploying anywhere else.

---

## References

- `ProductService/src/main/resources/application.properties`
- [`AuthenticationAndAuthorization.md`](AuthenticationAndAuthorization.md) — the
  Auth/User Service `userauthservice` is provisioned for.
