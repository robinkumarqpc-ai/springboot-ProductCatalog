package com.productservicing.productservice.Models.RepresentingInheritanceInDB.joined;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mentor_joined")
public class MentorJoined extends UserJoined {
    private Double avgRating;
    private String company;
}
