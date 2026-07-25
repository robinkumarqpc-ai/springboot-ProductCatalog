package com.productservicing.productservice.Models.RepresentingInheritanceInDB.tableperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mentor_table_per_class")
public class MentorTablePerClass extends UserTablePerClass {
    private Double avgRating;
    private String company;
}
