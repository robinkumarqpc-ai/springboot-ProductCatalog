package com.productservicing.productservice.Repository;

import com.productservicing.productservice.Models.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers only {@link ProductRepository#findProductWithGivenId(long)} - the one explicit
 * HQL {@code @Query} method in the repository. All other (derived) query methods are
 * intentionally out of scope for this test class.
 * <p>
 * {@code @DataJpaTest} loads only the JPA slice of the application context and rolls
 * back each test in a transaction, backed here by an in-memory H2 database instead of
 * the MySQL instance configured for the app.
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findProductWithGivenId_returnsProduct_whenIdExists() {
        Product product = new Product();
        product.setTitle("Wireless Mouse");
        product.setPrice(499.0);
        Product persisted = entityManager.persistAndFlush(product);

        Optional<Product> result = productRepository.findProductWithGivenId(persisted.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(persisted.getId());
        assertThat(result.get().getTitle()).isEqualTo("Wireless Mouse");
    }

    @Test
    void findProductWithGivenId_returnsEmpty_whenIdDoesNotExist() {
        Optional<Product> result = productRepository.findProductWithGivenId(-1L);

        assertThat(result).isEmpty();
    }
}
