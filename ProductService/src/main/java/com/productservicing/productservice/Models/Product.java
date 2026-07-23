package com.productservicing.productservice.Models;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
//@Data
public class Product extends BaseModel{
    //@Setter
    private String title;
    private Double price;
    private String description;
    private String imageURL;
    private Category category;
}
