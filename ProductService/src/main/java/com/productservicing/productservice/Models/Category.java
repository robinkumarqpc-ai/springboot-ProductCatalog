package com.productservicing.productservice.Models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
//@Data
@Entity(name="categories")
public class Category extends BaseModel{
    //@Column(unique=true,nullable=false)
    private String name;
    @OneToMany(mappedBy = "category", cascade = jakarta.persistence.CascadeType.REMOVE)//when its already mapped
    List<Product> products;

}
