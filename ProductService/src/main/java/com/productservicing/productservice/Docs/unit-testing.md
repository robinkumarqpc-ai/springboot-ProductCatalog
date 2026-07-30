# Unit Testing Notes

Notes on unit testing concepts and practices used in this project (JUnit 5, Mockito,
`spring-boot-starter-webmvc-test`, and `@DataJpaTest` with H2 — see the existing
`ProductRepositoryTest`).

## Why test?

Testing gives us confidence that the code behaves as expected, and that it keeps
behaving as expected as it changes. **Test-Driven Development (TDD)** flips the usual
order: identify and write the test cases first, then write the implementation to make
them pass.

## What to test

For any given method, cover three kinds of input:

| Scenario | Meaning | Example (`getArrElementByIndex(arr, x)`) |
|---|---|---|
| Positive | Input we typically expect from the caller | `0 <= x < arr.length` |
| Negative | Input we don't expect but must handle | `x < 0` or `x >= arr.length` |
| Edge case | Corner cases that tend to hide bugs | `arr == null` |

A test case is fundamentally: **sample input -> code -> actual output -> compare
against expected output.**

## The 3A pattern

Structure every test in three parts:

```java
@Test
void getProductByIdTest() {
    // Arrange - set up input and expected output
    long id = 10;
    Product expectedProduct = ...;

    // Act - call the method under test
    Product actual = productController.getProductById(id);

    // Assert - compare actual output against the expected output
    assertThat(actual).isEqualTo(expectedProduct);
}
```

Implementation can change; the behavior it's tested against shouldn't. **Test the
behavior, not the implementation** — e.g. a test for `sortArray` should keep passing
regardless of whether it's backed by merge sort or quick sort.

## Best practices for test cases

1. **Fast** — a slow suite doesn't get run.
2. **Isolated** — every test is completely independent; hard-code/mock dependencies
   instead of relying on shared or external state.
3. **Repeatable** — the same input produces the same output every time (no flakiness).
   A test whose result depends on timing, network calls, or ordering (e.g. asserting a
   DB call finishes within 10ms) is a **flaky test** — unreliable and to be avoided.
4. **Self-checking** — a test shouldn't require a human to eyeball the result; the
   expected output is part of the test itself.
5. **Test behavior, not implementation** (see above).

## Types of testing

```
        /--------------\
       /   Functional   \      complete end-to-end, no mocking
      /------------------\
     /    Integration     \    real dependencies, only external services mocked
    /----------------------\
   /       Unit Tests       \  one method in isolation, dependencies mocked
  /--------------------------\
```

- **Unit** — test each method in isolation; hard-code (mock) its dependencies so only
  that method's logic is exercised. `ProductRepositoryTest` in this repo is a unit test
  of a single `@Query` method, using `@DataJpaTest` + H2 to isolate the JPA slice from
  the rest of the app.
- **Integration** — call the real collaborators (e.g. a real `ProductService` backed by
  a real repository); only external dependencies (third-party APIs, payment gateways)
  get mocked. Slower than unit tests.
- **Functional** — full end-to-end flow (e.g. an order placement crossing Cart, Order,
  Inventory, Payment, and Notification services) with no mocking at all.
- **Code coverage** — the percentage of code actually executed by the test suite. High
  coverage is necessary but not sufficient for a good suite.

## Mocking and test doubles

**Mocking** lets us hard-code a dependency's behavior so the unit under test can be
exercised in isolation, independent of what that dependency actually does.

```java
@Mock
private ProductService productService;

@InjectMocks
private ProductController productController;

@Test
void getProductByIdTest() {
    long id = 10;
    Product expectedProduct = new Product();
    when(productService.getProductById(id)).thenReturn(expectedProduct);

    Product actual = productController.getProductById(id);

    assertThat(actual).isEqualTo(expectedProduct);
}
```

There's a spectrum of test doubles, moving from purely hard-coded to closer-to-real
behavior:

```
MOCK  --------------------------->  STUB  --------------------------->  FAKE
hard-coded                                                        closer to reality
```

- **Mock** — a double whose return values are hard-coded per call, with no dynamic
  behavior (`when(x).thenReturn(y)`). Good for isolating a single call's result.
- **Stub** — a hand-written class implementing the real dependency's interface, with
  simplified logic that replicates its observable behavior (e.g. a
  `ProductRepositoryStub` that just increments/returns a counter for `create()` /
  `count()`), letting you assert on behavior across multiple calls rather than a single
  canned return value.
- **Fake** — a stub taken further: a working, in-memory implementation of the real
  contract (e.g. a `ProductRepositoryFake` backed by a `Map<Integer, Product>` that
  actually assigns IDs and stores/retrieves objects). Closest to reality without the
  real infrastructure (no real DB, no real network).

This project's naming is a coincidence worth noting: `FakeStoreProductService` /
`FakeStoreProductDTO` integrate with the real fakestoreapi.com — "fake" there refers to
the third-party API's own name, not to the fake-test-double concept above.

For the broader taxonomy (dummy, fake, stub, spy, mock) and how these terms are used
across the industry, see: [What's the difference between faking, mocking, and
stubbing?](https://stackoverflow.com/questions/346372/whats-the-difference-between-faking-mocking-and-stubbing)

## MockMvc and JSON assertions

For controller-layer tests, `MockMvc` (from `spring-boot-starter-webmvc-test`) drives
the Spring MVC dispatcher without starting a real server, and lets you assert directly
on the JSON response:

```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void getProductById_returnsProductJson() throws Exception {
        when(productService.getProductById(10L)).thenReturn(sampleProduct());

        mockMvc.perform(get("/products/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Wireless Mouse"))
                .andExpect(jsonPath("$.price").value(499.0));
    }
}
```

## Reference implementation in this repo

`ProductService/src/test/java/com/productservicing/productservice/Repository/ProductRepositoryTest.java`
is a working example of the ideas above: it uses `@DataJpaTest` (JPA slice only, backed
by H2 instead of MySQL) and `TestEntityManager` to arrange data, calls the repository
method under test, and asserts against `AssertJ`'s `assertThat` — covering both the
positive (id exists) and negative (id doesn't exist) scenarios for the same method.
