package com.productservicing.productservice.Models.RepresentingInheritanceInDB.mappedsuperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "instructor_mapped_superclass")
public class InstructorMappedSuperclass extends UserMappedSuperclass {
    private String subject;
    private Integer noOfSessions;
}
