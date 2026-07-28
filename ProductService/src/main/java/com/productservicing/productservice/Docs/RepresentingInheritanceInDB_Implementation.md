# Representing Inheritance in DB — Implementation Notes

Companion to `RepresentingInheritanceInDB.MD`. This file is a pure reference note
covering the hands-on experiment — the package layout requested, and the mapping
from each Java package to the actual tables Hibernate creates. It is not code and
lives only in `Docs/`.

Location of the experiment:
`src/main/java/com/productservicing/productservice/Models/RepresentingInheritanceInDB/`

## Experiment Setup

Goal: experiment with all 4 JPA inheritance mapping strategies side by side, under
one package, isolated from the existing Product/Category models.

Structure: 4 sub-packages, one per strategy, each containing a `User` parent class
and 3 child classes (`Mentor`, `TA`, `Instructor`) implementing that strategy.

**Common fields:**
- `User` (parent): `id` (Long), `name` (String), `email` (String), `password` (String)
- `Mentor` (child): `avgRating` (Double), `company` (String)
- `TA` (child): `avgRating` (Double), `noOfMR` (Integer)
- `Instructor` (child): `subject` (String), `noOfSessions` (Integer)

Since all 4 strategies use the same `User`/`Mentor`/`TA`/`Instructor` names, each
package uses its own naming convention (class names and table names) to avoid
collisions:

1. **`mappedsuperclass` package**
   - `UserMappedSuperclass` (`@MappedSuperclass`, abstract, no table)
   - `MentorMappedSuperclass` (`@Entity`, table: `mentor_mapped_superclass`)
   - `TAMappedSuperclass` (`@Entity`, table: `ta_mapped_superclass`)
   - `InstructorMappedSuperclass` (`@Entity`, table: `instructor_mapped_superclass`)

2. **`joined` package**
   - `UserJoined` (`@Entity`, `@Inheritance(strategy = InheritanceType.JOINED)`, table: `users_joined`)
   - `MentorJoined` (`@Entity`, table: `mentor_joined`)
   - `TAJoined` (`@Entity`, table: `ta_joined`)
   - `InstructorJoined` (`@Entity`, table: `instructor_joined`)

3. **`tableperclass` package**
   - `UserTablePerClass` (`@Entity`, `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)`, table: `users_table_per_class`)
   - `MentorTablePerClass` (`@Entity`, table: `mentor_table_per_class`)
   - `TATablePerClass` (`@Entity`, table: `ta_table_per_class`)
   - `InstructorTablePerClass` (`@Entity`, table: `instructor_table_per_class`)

4. **`singletable` package**
   - `UserSingleTable` (`@Entity`, `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)`, `@DiscriminatorColumn(name = "user_type")`, table: `users_single_table`)
   - `MentorSingleTable` (`@Entity`, `@DiscriminatorValue("MENTOR")`)
   - `TASingleTable` (`@Entity`, `@DiscriminatorValue("TA")`)
   - `InstructorSingleTable` (`@Entity`, `@DiscriminatorValue("INSTRUCTOR")`)

**Requirements applied throughout:**
- Explicit `@Table(name = "...")` on every `@Entity`, so table names exactly match
  the naming convention above rather than relying on default naming.
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` on the id field
  wherever a real primary key is needed for that strategy.
- Lombok `@Getter`/`@Setter` on all classes.
- Spring Data JPA (`spring.jpa.hibernate.ddl-auto=update`, MySQL) auto-creates all
  tables on startup — no manual DDL.
- Fully isolated from the existing Product/Category models.

## Package → Table Mapping

All 4 sub-packages model the same `User` / `Mentor` / `TA` / `Instructor` hierarchy,
just with each strategy applied in isolation (separate class names and table names
per package, to avoid collisions).

### 1. `mappedsuperclass` package

Strategy: `@MappedSuperclass` — `UserMappedSuperclass` is abstract and **not** an entity.

Creates:
- `mentor_mapped_superclass`
- `ta_mapped_superclass`
- `instructor_mapped_superclass`

No parent table — each child table independently repeats `id`, `name`, `email`, `password`.

### 2. `joined` package

Strategy: `@Inheritance(strategy = InheritanceType.JOINED)` on `UserJoined`.

Creates:
- `users_joined` (parent — holds `id`, `name`, `email`, `password`)
- `mentor_joined` (child — `id` FK back to `users_joined.id`, plus `avgRating`, `company`)
- `ta_joined` (child — `id` FK back to `users_joined.id`, plus `avgRating`, `noOfMR`)
- `instructor_joined` (child — `id` FK back to `users_joined.id`, plus `subject`, `noOfSessions`)

### 3. `tableperclass` package

Strategy: `@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)` on `UserTablePerClass`.

Creates:
- `users_table_per_class` (parent — concrete, so it gets its own table, but Hibernate
  never writes a row into it directly; it only exists standalone, matching the
  conceptual write-up in `RepresentingInheritanceInDB.MD`)
- `mentor_table_per_class`
- `ta_table_per_class`
- `instructor_table_per_class`
- `users_table_per_class_seq` (a Hibernate-managed id-generator table, see note below)

`UserTablePerClass` is deliberately a **concrete** `@Entity`, not `abstract`. If it were
abstract (as it was in an earlier iteration of this experiment), Hibernate would skip
creating `users_table_per_class` entirely — an abstract class can never be instantiated,
so there'd be nothing to store a row for, and only the 3 concrete child tables would
appear. Making the parent concrete is what makes the "extra unused parent table" behavior
described in the conceptual doc actually happen.

Each child table (`mentor_table_per_class`, `ta_table_per_class`, `instructor_table_per_class`)
is self-contained, repeating `id`, `name`, `email`, `password` alongside its own fields —
same shape as `mappedsuperclass`.

Note on id generation: `GenerationType.IDENTITY` is **not allowed** here — Hibernate
rejects auto-increment ids for `TABLE_PER_CLASS` (`UnionSubclassEntityPersister`) because
per-table auto-increment can't guarantee unique ids across sibling child tables (this
applies regardless of whether the parent is abstract or concrete). This package uses
`GenerationType.AUTO` instead, which on MySQL resolves to a shared hi/lo-style counter
backed by the `users_table_per_class_seq` table.

### 4. `singletable` package

Strategy: `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` + `@DiscriminatorColumn(name = "user_type")` on `UserSingleTable`.

Creates:
- `users_single_table` — one table holding every column from every class
  (`id`, `name`, `email`, `password`, `avgRating`, `company`, `noOfMR`, `subject`,
  `noOfSessions`, `user_type`), with `user_type` set to `MENTOR`, `TA`, or `INSTRUCTOR`
  per row and the irrelevant columns left `NULL`.

## Total tables created by this experiment

13 tables across the 4 packages:

| Package            | Tables created                                                                 |
|---------------------|----------------------------------------------------------------------------------|
| `mappedsuperclass`  | `mentor_mapped_superclass`, `ta_mapped_superclass`, `instructor_mapped_superclass` |
| `joined`            | `users_joined`, `mentor_joined`, `ta_joined`, `instructor_joined`                |
| `tableperclass`     | `users_table_per_class` (parent, unused), `mentor_table_per_class`, `ta_table_per_class`, `instructor_table_per_class`, `users_table_per_class_seq` (id generator) |
| `singletable`       | `users_single_table`                                                             |