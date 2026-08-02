# Custom Authorization

Notes on the token-based authorization added to `ProductController#getSingleProduct`.

## What it does

`GET /product/{id}` now requires a `token` request header. `TokenValidation`
(a `@Component` in the `Utility` package) calls out to an external auth
service to validate it before the request is allowed to reach
`ProductService`.

```java
@GetMapping("/{id}")
public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId,
                                                  @RequestHeader("token") String tokenvalue) throws ProductNotFoundExceptions, InvalidTokenException {
    tokenValidation.validateToken(tokenvalue);
    ...
}
```

- Missing/invalid token -> `InvalidTokenException` -> handled by
  `ProductServiceExceptionHandler#handleInvalidTokenException` -> `401
  Unauthorized` with an `ExceptionDTO` body (`resolutionDetails: "Provide a
  valid token"`).
- Valid token -> request proceeds as before.

## TokenValidation

`ProductService/src/main/java/com/productservicing/productservice/Utility/TokenValidation.java`

Calls a separate auth service via `RestTemplate`:

```java
private static final String VALIDATE_TOKEN_URL = "http://localhost:8081/auth/validate";
```

Sends the token as a `token` header on a `GET` request; on any
`HttpStatusCodeException` (4xx/5xx from the auth service) it wraps the
failure as `InvalidTokenException("Invalid token provided")`. On success the
auth service's response body is deserialized into `UserDTO` (`id`, `name`,
`email`) and returned, though the controller currently discards it — it only
uses `validateToken` as a gate.

**This is a hardcoded localhost URL** — pull it into `application.properties`
before this goes anywhere beyond local dev.

## Wiring

- `ProductController` now takes `TokenValidation` as a second constructor
  argument (in addition to `ProductService`), so it's a required Spring bean.
- `RestTemplate` itself isn't defined as a `@Bean` in the codebase yet — one
  needs to exist somewhere (e.g. a `@Configuration` class) for
  `TokenValidation`'s constructor injection to resolve.

## Test impact

Both `ProductControllerTest` (`@SpringBootTest` + `@MockitoBean`) and
`ProductControllerMVCTest` (`@WebMvcTest`) now also `@MockitoBean` mock
`TokenValidation`, and pass a dummy token value (e.g. `"dummy-token"`) when
calling `getSingleProduct` directly, since the real bean would otherwise try
to reach `http://localhost:8081/auth/validate`.

Following this repo's [comment, don't replace] convention, the old
no-token/no-`ResponseEntity` version of `getSingleProduct` is left commented
out above the new one in `ProductController.java` rather than deleted.