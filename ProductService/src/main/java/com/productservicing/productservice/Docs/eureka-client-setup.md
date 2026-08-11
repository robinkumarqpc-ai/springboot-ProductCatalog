# Eureka Client Setup

Notes on registering `ProductService` as a Eureka client with the `ServiceDiscovery`
Eureka server (separate repo: `springboot-portfolio/ServiceDiscovery`), following
[Spring Cloud Netflix Eureka](https://www.baeldung.com/spring-cloud-netflix-eureka).

## What it does

On startup, `ProductService` registers itself with the Eureka server as `PRODUCTSERVICE`
(derived from `spring.application.name`), then sends a heartbeat every ~30s to stay
registered. This lets other services in the portfolio (`PaymentService`,
`UserAuthService`, etc.) discover and call it by name instead of a hardcoded
host/port, once they also add load-balanced clients.

`ServiceDiscovery` itself is a standalone Eureka server (`server.port=8761`,
`eureka.client.register-with-eureka=false`, `eureka.client.fetch-registry=false`)
— it does not register with itself, it only hosts the registry.

## Dependencies (`pom.xml`)

```xml
<properties>
    <spring-cloud.version>2025.1.2</spring-cloud.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
</dependencies>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

`spring-cloud.version` must stay aligned with the Spring Boot parent version
(`4.1.0` here). `ServiceDiscovery` uses the same Spring Cloud BOM version, so both
projects stay compatible.

## Configuration (`application.properties`)

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true
```

- `defaultZone` - where the Eureka server lives. Must match `ServiceDiscovery`'s
  `server.port` (`8761`) and its `/eureka/` endpoint.
- `prefer-ip-address` - registers with the machine's IP instead of hostname, which
  is more reliable for local multi-service setups and Docker.
- `spring.application.name=ProductService` (already set) becomes the registered
  service ID (`PRODUCTSERVICE`) other clients will look it up by.

## No code changes needed

Older guides use `@EnableEurekaClient` on the `@SpringBootApplication` class. This
is no longer required: Spring Boot auto-configures the Eureka client purely from
the starter dependency being on the classpath. `ProductServiceApplication` is
untouched.

## Running / verifying

1. Start `ServiceDiscovery` first (port `8761`).
2. Start `ProductService` (port `8080`).
3. Open `http://localhost:8761` - within ~30s, `PRODUCTSERVICE` should appear
   under "Instances currently registered with Eureka".