package com.productservicing.productservice.Models.RepresentingInheritanceInDB.singletable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("MENTOR")
public class MentorSingleTable extends UserSingleTable {
    private Double avgRating;
    private String company;
}
