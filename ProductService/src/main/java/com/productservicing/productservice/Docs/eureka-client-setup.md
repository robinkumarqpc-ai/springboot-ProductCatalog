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
2. Start `ProductService` (port `8080` by default - see note below).
3. Open `http://localhost:8761` - within ~30s, `PRODUCTSERVICE` should appear
   under "Instances currently registered with Eureka".

### Running multiple instances (`SERVER_PORT`)

`server.port=${SERVER_PORT:8080}` in `application.properties` reads the port from
an env var, falling back to `8080` if unset. Running the same jar with a
different `SERVER_PORT` registers as a second, distinct instance under the same
`PRODUCTSERVICE` application name - Eureka differentiates instances by
`instanceId` (`host:appName:port`), so a different port is enough.

In IntelliJ: **Run > Edit Configurations > (select config) > Modify options >
Environment variables**, then add `SERVER_PORT=8081` (copy the run config for
each additional instance/port).

## Client-side load balancing demo (`service-discoverablity-test`)

A minimal end-to-end example of `ProductService` calling `UserAuthService`
*by its Eureka service name* instead of a hardcoded host/port, and having the
call load-balanced across however many `UserAuthService` instances are
registered at the time.

### `UserAuthService` side

- `Controller/ServiceDiscoverablityTestController.java` - `GET
  /service-discoverablity-test/ping`, returns `"Hi, I am UserAuthService,
  running on port <port>"` (port included so you can see which instance
  answered).
- `Security/SecurityConfig.java` - added
  `.requestMatchers("/service-discoverablity-test/**").permitAll()` ahead of
  the existing `.anyRequest().authenticated()` catch-all, since the default
  chain otherwise requires auth on every endpoint.
- Its `server.port=${USER_SERVICE_SERVER_PORT}` has **no default** - it must be
  set (e.g. `9090`, `9091`, `9092`...) per run configuration to run multiple
  instances, same pattern as `SERVER_PORT` above.

### `ProductService` side

- `pom.xml` - added `spring-cloud-starter-netflix-eureka-client`'s sibling
  starter, `spring-cloud-starter-loadbalancer`. This is what actually resolves
  a logical service name (`USERAUTHSERVICE`) to a real host:port pulled from
  the Eureka registry - the Eureka client alone only maintains the registry
  cache, it doesn't do the resolving/balancing.
- `Configuration/ApplicationConfig.java` - the existing `createRestTemplateBean()`
  (used by `FakeStoreProductService` and `Utility/TokenValidation`, which call
  fixed/external URLs) is now marked `@Primary` so those keep resolving
  unambiguously. A **second**, separate bean, `loadBalancedRestTemplate()`, is
  annotated `@LoadBalanced` - that annotation is what makes this particular
  `RestTemplate` resolve service names via Eureka instead of dispatching the
  HTTP call as-is.
- `Controllers/ServiceDiscoverablityTestController.java` - `GET
  /service-discoverablity-test/call-user-auth`, calls
  `http://USERAUTHSERVICE/service-discoverablity-test/ping` via the
  load-balanced `RestTemplate` (injected with
  `@Qualifier("loadBalancedRestTemplate")`) and returns the response.

### Trying it

1. Start `ServiceDiscovery`, then multiple `UserAuthService` instances (e.g.
   `USER_SERVICE_SERVER_PORT=9090`, `9091`, `9092`), then `ProductService`.
2. Direct pings (bypass discovery, hit one instance each):
   `http://localhost:9090/service-discoverablity-test/ping`,
   `.../9091/...`, `.../9092/...`.
3. The actual load-balancing test - call this repeatedly and watch the port in
   the response body rotate across instances:
   `http://localhost:8080/service-discoverablity-test/call-user-auth`.