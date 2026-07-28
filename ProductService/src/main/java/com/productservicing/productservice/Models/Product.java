package com.productservicing.productservice.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity//(name="product")
public class Product extends BaseModel{
    //@Setter
    private String title;
    private Double price;
    private String description;
    private String imageURL;
    @ManyToOne //If not defined it fails at Compile - time , Since category is not plain object rather an entity
    private Category category;
}
