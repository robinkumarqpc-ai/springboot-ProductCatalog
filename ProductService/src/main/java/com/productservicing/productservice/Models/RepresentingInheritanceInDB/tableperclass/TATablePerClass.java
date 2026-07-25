package com.productservicing.productservice.Models.RepresentingInheritanceInDB.tableperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ta_table_per_class")
public class TATablePerClass extends UserTablePerClass {
    private Double avgRating;
    private Integer noOfMR;
}
