package com.productservicing.productservice.Models.RepresentingInheritanceInDB.tableperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "instructor_table_per_class")
public class InstructorTablePerClass extends UserTablePerClass {
    private String subject;
    private Integer noOfSessions;
}
