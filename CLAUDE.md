# CommaFeed Project Guide

## Stack

- Java 21
- Quarkus
- H2 Database for the default embedded profile

## Architecture conventions

Backend request handling follows the layered flow:

```text
REST -> Service -> DAO -> Entity
```

REST resources handle HTTP concerns and status codes. Services contain business
logic, DAOs encapsulate persistence queries, and entities define the JPA
database mappings.

## Standard commands

Run these commands from the project root on Windows:

```powershell
.\mvnw.cmd clean package -pl commafeed-server "-Dmaven.compiler.release=21"
.\mvnw.cmd test -pl commafeed-server "-Dmaven.compiler.release=21"
.\mvnw.cmd checkstyle:check -pl commafeed-server
.\mvnw.cmd spotless:check -pl commafeed-server
.\mvnw.cmd spotless:apply -pl commafeed-server
```

On Unix-like systems, use `./mvnw` instead of `.\mvnw.cmd`.
