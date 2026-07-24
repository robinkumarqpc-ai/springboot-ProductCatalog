package com.productservicing.productservice.Models;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
//@Data
@Entity(name="categories")
public class Category extends BaseModel{
    private String name;

}
