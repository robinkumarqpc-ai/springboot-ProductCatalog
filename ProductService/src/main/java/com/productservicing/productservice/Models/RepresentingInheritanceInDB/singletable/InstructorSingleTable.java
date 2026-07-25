package com.productservicing.productservice.Models.RepresentingInheritanceInDB.singletable;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("INSTRUCTOR")
public class InstructorSingleTable extends UserSingleTable {
    private String subject;
    private Integer noOfSessions;
}
