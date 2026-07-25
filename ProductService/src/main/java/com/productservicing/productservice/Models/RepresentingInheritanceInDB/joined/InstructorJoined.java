package com.productservicing.productservice.Models.RepresentingInheritanceInDB.joined;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "instructor_joined")
public class InstructorJoined extends UserJoined {
    private String subject;
    private Integer noOfSessions;
}
