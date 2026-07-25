package com.productservicing.productservice.Models.RepresentingInheritanceInDB.mappedsuperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mentor_mapped_superclass")
public class MentorMappedSuperclass extends UserMappedSuperclass {
    private Double avgRating;
    private String company;
}
