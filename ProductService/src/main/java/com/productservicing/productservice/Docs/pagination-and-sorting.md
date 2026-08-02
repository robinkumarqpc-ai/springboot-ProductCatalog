# Pagination and Sorting

Notes on the `GET /product/search` endpoint, which finds products by (partial)
title with server-side sorting and pagination via Spring Data's `Pageable`.

## What it does

```
GET /product/search?title=shirt&pageNumber=0&pageSize=10&sortBy=title&sortDirection=asc
```

- `title` (required) - substring match, case-insensitive.
- `pageNumber` (default `0`) - zero-indexed page.
- `pageSize` (default `10`) - results per page.
- `sortBy` (default `title`) - field to sort by.
- `sortDirection` (default `asc`) - `asc` or `desc`.

```java
@GetMapping("/search")
public ResponseEntity<Page<Product>> getProductsByTitle(
        @RequestParam("title") String title,
        @RequestParam(value = "pageNumber", defaultValue = "0") int pageNumber,
        @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
        @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
        @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection) {
    return new ResponseEntity<>(
            this.productService.getProductsByTitle(title, pageNumber, pageSize, sortBy, sortDirection),
            HttpStatus.OK
    );
}
```

The response body is a Spring Data `Page<Product>` - besides `content`, it
serializes paging metadata (`totalElements`, `totalPages`, `number`, `size`,
`sort`, `first`/`last`, etc.), so clients don't need a separate count call.

## Wiring

`ProductService#getProductsByTitle(title, pageNumber, pageSize, sortBy, sortDirection)`
is implemented differently per backend, since only one of them is backed by a
real database:

### `StorageProductService` (JPA)

Sorting and pagination are pushed down to the database via `Pageable`:

```java
Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.ASC);
PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(direction, sortBy));
return this.productRepository.findByTitleContainsIgnoreCase(title, pageRequest);
```

This relies on a new overload added to `ProductRepository`, alongside the
existing non-paged derived query:

```java
List<Product> findByTitleContainsIgnoreCase(String title);
Page<Product> findByTitleContainsIgnoreCase(String title, Pageable pageable);
```

Spring Data JPA supports overloading a derived query method purely by adding
a trailing `Pageable`/`Sort` parameter - same query, same matching rules,
just with paging/sorting applied by the database (`ORDER BY ... LIMIT ...
OFFSET ...`).

**Caveat:** `sortBy` is passed straight through to `Sort.by(direction,
sortBy)` with no whitelist against `Product`'s fields. An unknown field name
produces a `PropertyReferenceException` (HTTP 500) rather than a clean 400 -
acceptable for now since this is an internal/demo API, but worth tightening
before this is exposed publicly.

### `FakeStoreProductService` (external API, `@Primary`)

This service has no database to push the query down to - it proxies
`https://fakestoreapi.com`, which has no title-search, sort, or pagination
parameters of its own. So filtering/sorting/pagination happen in memory over
the full product list returned by `getAllProducts()`:

```java
List<Product> filteredProducts = getAllProducts().stream()
        .filter(product -> product.getTitle() != null && product.getTitle().toLowerCase().contains(title.toLowerCase()))
        .collect(Collectors.toList());

Comparator<Product> comparator = switch (sortBy) {
    case "price" -> Comparator.comparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder()));
    case "id" -> Comparator.comparing(Product::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    case "description" -> Comparator.comparing(Product::getDescription, Comparator.nullsLast(Comparator.naturalOrder()));
    default -> Comparator.comparing(Product::getTitle, Comparator.nullsLast(Comparator.naturalOrder()));
};
if ("desc".equalsIgnoreCase(sortDirection)) {
    comparator = comparator.reversed();
}
filteredProducts.sort(comparator);
```

The page slice is then built manually and wrapped in a `PageImpl`, which is
the concrete `Page` implementation you construct yourself when you already
have the content and total count in hand (as opposed to `Page` instances
returned by a `PagingAndSortingRepository`, which come pre-built):

```java
int fromIndex = Math.min(pageNumber * pageSize, filteredProducts.size());
int toIndex = Math.min(fromIndex + pageSize, filteredProducts.size());
List<Product> pageContent = filteredProducts.subList(fromIndex, toIndex);

return new PageImpl<>(pageContent, PageRequest.of(pageNumber, pageSize), filteredProducts.size());
```

Unlike the JPA path, an unrecognized `sortBy` here silently falls back to
sorting by `title` (the `switch` expression's `default` case) rather than
erroring.

### Why both implementations matter

`FakeStoreProductService` is `@Primary`, so it's what `ProductController`
actually injects by default - `StorageProductService` only gets used if
explicitly `@Qualifier`'d in. Both implement the full `ProductService`
interface (a Java interface makes every method mandatory on every
implementer), so `getProductsByTitle` had to be added to both, not just
whichever one the controller happens to wire up today.

## Related endpoints

- `GET /product` (`getAllProducts`) - unpaged, returns every product as a
  plain `List<Product>`. `getProductsByTitle` is a separate, additive
  endpoint (`/product/search`) rather than a replacement.