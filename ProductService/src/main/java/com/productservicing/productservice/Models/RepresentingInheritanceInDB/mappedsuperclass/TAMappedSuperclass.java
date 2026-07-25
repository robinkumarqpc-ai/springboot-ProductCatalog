package com.productservicing.productservice.Models.RepresentingInheritanceInDB.mappedsuperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ta_mapped_superclass")
public class TAMappedSuperclass extends UserMappedSuperclass {
    private Double avgRating;
    private Integer noOfMR;
}
