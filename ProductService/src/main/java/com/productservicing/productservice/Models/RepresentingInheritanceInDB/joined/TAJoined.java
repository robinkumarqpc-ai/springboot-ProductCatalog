package com.productservicing.productservice.Models.RepresentingInheritanceInDB.joined;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "ta_joined")
public class TAJoined extends UserJoined {
    private Double avgRating;
    private Integer noOfMR;
}
