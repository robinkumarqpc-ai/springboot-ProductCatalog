# DB Migrations

**What is a DB migration?**
Just like we maintain a history of code changes with Git (`C0 → C1 → C2 → C3 → ...`), 
we should also maintain a history of **schema changes** to a database (`S1 → S2 → S3 → S4 → ...`). A migration is simply a small, ordered, repeatable script that moves a database schema from one known version to the next — e.g. "add a `description` column to `products`," "add a `NOT NULL` constraint," "rename `qty` to `quantity`." Run in order, migrations turn a blank database into today's schema, one deterministic step at a time.

**Why not just let Hibernate auto-create/update the schema?**
Spring Data JPA's `spring.jpa.hibernate.ddl-auto=update` (used in this project) is convenient for local development — Hibernate looks at your `@Entity` classes and generates the schema for you. But it falls apart as a real strategy:
- **No history** — there's no record of *what* changed and *when*, only the current end state.
- **Not reviewable** — schema changes aren't expressed as a diff you can put in a PR.
- **Unsafe on real data** — Hibernate can add columns, but it won't safely rename a column, backfill data, or drop something without risking data loss. It infers *structure*, not *intent*.
- **Not reproducible across environments** — dev, staging, and prod can silently drift out of sync.

As a developer, you need **explicit control** over how the schema evolves — 
that's the problem schema migration tools solve. 
In practice: use `ddl-auto=validate` (or `none`) once migrations are in place, and let the migration tool — not Hibernate — own schema changes.

---

## Core idea: the schema has versions, like code has commits

```
Code history:      C0 → C1 → C2 → C3
Schema history:     S1 → S2 → S3 → S4
```

Each `Sn` is one migration script. The migration tool applies them **in order**, and — critically — tracks *which ones have already run* in a metadata table inside the database itself, so re-running the app never re-applies a migration twice.

Example version-tracking table (conceptually — Flyway calls this `flyway_schema_history`):

| version | description   | timestamp   | status  |
|---------|----------------|-------------|---------|
| V1      | init           | 12 May '25  | SUCCESS |
| V2      | add column     | 15 May '25  | SUCCESS |
| V3      | add index      | —           | PENDING |

On every startup, the tool compares this table against the migration files on disk and applies only the ones that haven't run yet.

---

## Popular migration tools

| Tool | Format | Notes |
|---|---|---|
| **Flyway** | Plain `.sql` files (or Java for advanced cases) | Simplest mental model — SQL you write yourself. Has first-class Spring Boot auto-configuration (`spring-boot-starter-data-jpa` + `flyway-core` on the classpath is enough — it runs automatically on startup). |
| **Liquibase** | XML / YAML / JSON / SQL "changelogs" | More abstraction — changesets are database-agnostic and can be rolled back declaratively. Steeper learning curve, more powerful for multi-DB support. |

For a single-database Spring Boot app like this one, **Flyway** is the more common default because the migrations are just SQL — no new DSL to learn.

---

## Flyway naming convention & folder layout

Migrations live under `src/main/resources/db/migration` and follow a strict naming pattern so Flyway can order and identify them:

```
V<version>__<description>.sql
```

```
db/migration/
├── V1__init.sql
├── V2__add_description_to_product.sql
├── V3__add_index_on_category_name.sql
```

- `V1`, `V2`, `V3` — the version number, must increase, must be unique.
- Double underscore (`__`) — separates version from description (mandatory, exactly two underscores).
- Description — free text, used only for readability (shown in the history table).
- `.sql` — the file just contains plain DDL/DML:

```sql
-- V1__init.sql
CREATE TABLE product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price DOUBLE,
    description VARCHAR(255)
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
```

```sql
-- V2__add_description_to_product.sql
ALTER TABLE product ADD COLUMN stock INT DEFAULT 0;
```

**Rules that matter in practice:**
- Once a migration has been applied (recorded in the history table), **never edit that file** — Flyway checksums each migration and will refuse to start if an already-applied file changes. If you got it wrong, write a *new* migration (`V3__fix_x.sql`) that corrects it.
- Migrations are applied in strict numeric order, and once `Vn` has run, you generally can't insert a new `V(n-1)` after the fact.
- Migrations should be small and additive where possible — one logical schema change per file.

---

## How a Java type maps to a schema type (why migrations exist at all)

This is the same translation Hibernate does implicitly with `ddl-auto` — a migration just makes it explicit and reviewable:

```
int     → INT / INTEGER
String  → VARCHAR(255)   (Hibernate's default length)
Double  → DOUBLE
Long    → BIGINT
Date    → DATE / DATETIME / TIMESTAMP
```

With `ddl-auto=update`, Hibernate infers this mapping and issues the `ALTER TABLE` for you, silently, on startup. With a migration tool, *you* write the `ALTER TABLE`, so you control exactly what happens — including things Hibernate won't safely do, like renaming a column or backfilling a new `NOT NULL` field with a default for existing rows.

---

## Where this fits in this project

Currently `application.properties` has:
```properties
spring.jpa.hibernate.ddl-auto=update
```
This is fine for the current experimentation stage (e.g. the [[RepresentingInheritanceInDB]] work), where entity shapes are still changing frequently. The moment this project has data worth preserving across restarts / deployments, the move is:

1. Add `flyway-core` (and `flyway-mysql` for MySQL-specific support) as a dependency.
2. Set `spring.jpa.hibernate.ddl-auto=validate` — Hibernate then only *checks* that entities match the schema, it never mutates it.
3. Write the current schema as `V1__init.sql` under `src/main/resources/db/migration`.
4. Every future schema change becomes a new `V2__...`, `V3__...` file instead of relying on Hibernate to infer it.

---

## Quick recap

- **Why:** schema needs a history/versioning story the same way code does — auto-generated DDL isn't reviewable, safe, or reproducible.
- **How:** ordered, immutable, numbered scripts (`V1`, `V2`, ...), tracked in a metadata table inside the DB so each one runs exactly once.
- **Tools:** Flyway (plain SQL, simple) or Liquibase (changelogs, more abstraction/rollback support).
- **Golden rule:** never edit an already-applied migration — add a new one instead.