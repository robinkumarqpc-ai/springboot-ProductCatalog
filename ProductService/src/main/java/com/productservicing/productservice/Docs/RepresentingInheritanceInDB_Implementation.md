# Representing Inheritance in DB — Implementation Notes

Companion to `RepresentingInheritanceInDB.MD`. This file is a pure reference note
mapping the experiment's Java packages to the actual tables Hibernate creates —
it is not code and lives only in `Docs/`.

Location of the experiment:
`src/main/java/com/productservicing/productservice/Models/RepresentingInheritanceInDB/`

All 4 sub-packages model the same `User` / `Mentor` / `TA` / `Instructor` hierarchy,
just with each strategy applied in isolation (separate class names and table names
per package, to avoid collisions).

## 1. `mappedsuperclass` package

Strategy: `@MappedSuperclass` — `UserMappedSuperclass` is abstract and **not** an entity.

Creates:
- `mentor_mapped_superclass`
- `ta_mapped_superclass`
- `instructor_mapped_superclass`

No parent table — each child table independently repeats `id`, `name`, `email`, `password`.

## 2. `joined` package

Strategy: `@Inheritance(strategy = InheritanceType.JOINED)` on `UserJoined`.

Creates:
- `users_joined` (parent — holds `id`, `name`, `email`, `password`)
- `mentor_joined` (child — `id` FK back to `users_joined.id`, plus `avgRating`, `company`)
- `ta_joined` (child — `id` FK back to `users_joined.id`, plus `avgRating`, `noOfMR`)
- `instructor_joined` (child — `id` FK back to `users_joined.id`, plus `subject`, `noOfSessions`)

## 3. `tableperclass` package

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

## 4. `singletable` package

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
