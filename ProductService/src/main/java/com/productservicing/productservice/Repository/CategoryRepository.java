package com.productservicing.productservice.Repository;

import com.productservicing.productservice.Models.Category;
import com.productservicing.productservice.Models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long>  {
   Optional<Category> findByName(String name);
   @Override
   void deleteById(Long id);
}
