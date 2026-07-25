package com.productservicing.productservice.Models.RepresentingInheritanceInDB.tableperclass;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users_table_per_class")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class UserTablePerClass {
    // IDENTITY is not allowed here: Hibernate can't use per-table auto-increment
    // across a TABLE_PER_CLASS union, since ids must stay unique across all child tables.
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String email;
    private String password;
}
