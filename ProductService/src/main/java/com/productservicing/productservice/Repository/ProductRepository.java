package com.productservicing.productservice.Repository;

import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Product}, backed by Spring Data JPA.
 * <p>
 * Two query strategies are used here:
 * <ol>
 *     <li><b>Derived query methods</b> - Spring parses the method name (e.g.
 *     {@code findByTitleContainsIgnoreCase}) and generates the query automatically.
 *     No implementation is written; the method signature alone is the contract.
 *     Reference: <a href="https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html">
 *     Spring Data JPA - Query Methods</a>.</li>
 *     <li><b>HQL (Hibernate Query Language) via {@code @Query}</b> - used when a derived
 *     name would be unwieldy or the query needs more control. HQL looks like SQL but
 *     operates on entity names/fields, not table/column names (e.g. {@code Product}, not
 *     {@code product}), so it stays portable across databases. Set
 *     {@code @Query(nativeQuery = true)} instead if a real, database-specific SQL
 *     statement is ever needed.</li>
 * </ol>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    /**
     * Derived query. Equivalent SQL: {@code select * from product where id = ?}.
     */
    @Override
    Optional<Product> findById(Long productId);

    /**
     * Derived query with two keywords chained together:
     * {@code Contains} -> {@code like '%title%'}, {@code IgnoreCase} -> {@code lower(...)}.
     * Equivalent SQL: {@code select * from product where lower(title) like lower('%title%')}.
     * <p>
     * Returns a {@code List}, not {@code Optional<List<Product>>} - Spring Data always
     * returns an empty list (never null) when there are no matches, so there is nothing
     * to guard with {@code Optional}.
     */
    List<Product> findByTitleContainsIgnoreCase(String title);

    /**
     * Derived query. {@code Between} maps to a SQL {@code BETWEEN} range, inclusive on
     * both ends. Equivalent SQL:
     * {@code select * from product where price between ? and ?}.
     */
    List<Product> findByPriceBetween(Double priceAfter, Double priceBefore);

    /**
     * Derived query. Spring recognises the {@code @ManyToOne}/{@code @OneToMany} relation
     * to {@link Category} and joins on the foreign key. Equivalent SQL:
     * {@code select * from product where category_id = ?}.
     */
    List<Product> findByCategory(Category category);

    /**
     * Derived query navigating into the related entity's {@code id} field via the
     * {@code _} nested-property separator. Equivalent SQL:
     * {@code select * from product where category_id = ?}.
     */
    List<Product> findAllByCategory_Id(Long categoryId);

    /**
     * Derived query that requires an implicit JOIN against {@link Category}, since
     * {@code name} lives on the related entity, not on {@code Product} itself.
     * Equivalent SQL:
     * {@code select p.* from product p join category c on p.category_id = c.id where c.name = ?}.
     */
    List<Product> findAllByCategory_Name(String categoryName);

    /**
     * Upsert: inserts a new row if the entity's id is null/absent, otherwise updates the
     * existing row matching the id.
     */
    @Override
    Product save(Product product);

    @Override
    void deleteById(Long productId);

    Product getProductById(Long id);

    /**
     * Explicit HQL query (see class-level Javadoc for HQL vs. native SQL). {@code Product}
     * here refers to the entity, and {@code p.id} to its mapped field - not the physical
     * table/column names, which is what makes HQL database-agnostic.
     * {@code @Param("id")} binds the method parameter to the {@code :id} named parameter
     * in the query string.
     */
    @Query("select p from Product p  where p.id = :id")
    Optional<Product> findProductWithGivenId(@Param("id") long productId);
    //SQLnative query
    @Query(value = "select * from Product  where id = :id",nativeQuery = true)
    Optional<Product> findProductWithGivenIdNative(@Param("id") long productId);

}
