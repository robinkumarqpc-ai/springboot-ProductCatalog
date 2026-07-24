package com.productservicing.productservice.Repository;

import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html
@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {


    //select * from product where id = 'productId'
    @Override
    //@NullMarked
    Optional<Product> findById(Long productId);

    //select * from product where lower(title) like '%title%'
    List<Product> findByTitleContainsIgnoreCase(String title);
    //not Optional<List<Product>> since even
    // when no result list will be just empty not null

    //find all products price>=100 and <=1000
    //@Query()-To Override hibernate
    List<Product> findByPriceBetween(Double priceAfter,Double priceBefore);
    //select * from product where category_id = category.id
    List<Product> findByCategory(Category category);

    List<Product> findAllByCategory_Id(Long categoryId);
    //JOIN query
    List<Product> findAllByCategory_Name(String categoryName);
    @Query("select title from Product  where id = ?")
    Optional<Product> findProductTitleById(Long productId);

}
