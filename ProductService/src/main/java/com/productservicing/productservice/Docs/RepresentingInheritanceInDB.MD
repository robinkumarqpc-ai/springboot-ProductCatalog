# Representing Inheritance in DB (JPA/Hibernate)

**What does "representing inheritance in DB" mean?**
In Java, classes can inherit from one another — a `Mentor`, `TA`, and `Instructor` can all extend a common `User` class, sharing fields like `id`, `name`, `email`, `password`, while also having their own unique fields. But relational databases don't have a native concept of "inheritance" — a table is just a flat set of columns. So when you use JPA/Hibernate to save these Java objects to a database, you have to pick a **strategy for how that class hierarchy gets translated into actual tables and columns**. That's what "representing inheritance in DB" refers to — the four strategies below are the different ways JPA lets you map an inheritance hierarchy onto relational tables.

**Extra note — what is a polymorphic query?**
A polymorphic query is any query written against the **parent type**, that transparently returns instances of **all its subclasses** combined — e.g., writing `SELECT u FROM User u` and getting back a mix of `Mentor`, `TA`, and `Instructor` objects, without needing to query each subclass separately. Whether this is possible (and how expensive it is) depends entirely on which inheritance mapping strategy is used below.

**Example used throughout:** a `User` parent class, with three child types — `Mentor`, `TA` (Teaching Assistant), and `Instructor`.

```
User (parent)
├── id
├── name
├── email
└── password

Mentor (child)          TA (child)              Instructor (child)
├── avgRating           ├── avgRating           ├── subject
└── company             └── noOfMR              └── noOfSessions
```

---

## 1. Mapped Superclass

**What it does:**
- Used when the parent class (`User`) **isn't a real entity** — it's just an **abstract** base holding shared fields.
- **No table is created for `User`.**
- **A table is created for each child class** (`mentor`, `ta`, `instructor`), and each one contains **all** attributes — both its own fields and everything inherited from `User`.
- Since there's no shared parent table, querying across all children requires a **UNION** of each child table.

**Resulting tables:**
```
mentor:     id | name | email | password | avgRating | company
ta:         id | name | email | password | noOfMR    | avgRating
instructor: id | name | email | password | subject   | noOfSessions
```

**Example query — "Get the email of all the users":**
```sql
SELECT email FROM tas
UNION
SELECT email FROM mentors
UNION
SELECT email FROM instructors;
```

**Code — Example 1 (User / Mentor / TA / Instructor):**
```java
@MappedSuperclass
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

**Code — Example 2 (generic `BaseModel`, common in real projects):**
```java
@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt;
    private Date lastModifiedAt;
}

@Entity
public class Product extends BaseModel {
    private String title;
    private Double price;
    private String description;
}

@Entity
public class Category extends BaseModel {
    private String name;
}
```

Same idea as the `User` example — `BaseModel` isn't a real entity, `Product` and `Category` each get their own table, and both tables independently include `id`, `createdAt`, and `lastModifiedAt` alongside their own fields.

**Resulting tables (Example 2):**
```
product:  id | created_at | last_modified_at | title | price | description
category: id | created_at | last_modified_at | name
```

---

## 2. Joined Table *(most frequently used way)*

**What it does:**
- Creates **one table for the parent class** (`users`), and **one table for each child class**.
- The `users` table holds the shared fields (`id`, `name`, `email`, `password`).
- Each child table (`tas`, `mentors`, `instructor`) holds **only its own extra fields**, plus a `userId` column that's a **foreign key back to `users.id`**.
- To fetch a full child object, Hibernate performs a **JOIN** between `users` and that child's table.
- Most **normalized** option — no duplicate/wasted columns anywhere, and polymorphic queries work naturally off the `users` table.

**Resulting tables:**
```
users:      id | name | email | password
tas:        userId (FK -> users.id) | noOfMR | avgRating
mentors:    userId (FK -> users.id) | company | avgRating
instructor: userId (FK -> users.id) | subject | noOfSession
```

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

---

## 3. Table Per Class

**What it does:**
- **Exactly the same as Mapped Superclass** — the only difference is that here, we **also create a table for the parent class** (`users`), even though it isn't used to link the children together.
- Each child table (`mentor`, `ta`, `instructor`) is still **fully self-contained**, repeating the parent's columns.
- Polymorphic queries across children are possible via a **UNION**, same as Mapped Superclass — the standalone `users` table doesn't actually merge data from the children.

**Resulting tables:**
```
users:      id | name | email | password
mentor:     id | name | email | password | avgRating | company
ta:         id | name | email | password | noOfMR    | avgRating
instructor: id | name | email | password | subject   | noOfSessions
```

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

**Implementation gotcha (Hibernate):**
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` is **not allowed** here — Hibernate
  throws `MappingException: Cannot use identity column key generation with <union-subclass>
  mapping`. Per-table auto-increment can't guarantee unique ids across sibling child tables
  once they're combined, so use `GenerationType.AUTO` (or `SEQUENCE`/`TABLE`) instead.
- The `users` table above only appears if `User` is a **concrete** class. If `User` is left
  `abstract`, Hibernate has nothing to instantiate for it and skips creating its table
  entirely — you'd get only the 3 child tables, same as Mapped Superclass.

---

## 4. Single Table

**What it does:**
- Creates **just ONE table** (`users`) with **all attributes from all classes** combined.
- Becomes a **sparse table** — lots of **NULLs**, since a `Mentor` row won't have a `subject` or `noOfSessions`, a `TA` row won't have `company`, etc.
- Uses a **`userType` discriminator column** to record which subclass each row actually represents:
  - `TA` → `1`
  - `Mentor` → `2`
  - `Instructor` → `3`
- **Fastest option** — no joins, no unions, everything lives in one table.

**Resulting table:**
```
users: id | name | email | password | noOfMR | avgRating | company | subject | noOfSessions | userType
```
(each row only fills in the columns relevant to its `userType`; everything else is NULL)

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "userType")
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
@DiscriminatorValue("2")
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
@DiscriminatorValue("1")
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
@DiscriminatorValue("3")
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

---

## Quick Comparison

| Strategy          | # Tables                           | Attribute/key structure                                                   | Polymorphic queries      | Nulls?        | Joins needed?               |
|--------------------|-------------------------------------|-----------------------------------------------------------------------------|----------------------------|----------------|--------------------------------|
| Mapped Superclass  | 1 per child (no parent table)       | Parent's attrs **duplicated** into every child table, each with its own `id` | Not possible (UNION only)  | No             | No                             |
| Joined             | 1 parent (`users`) + 1 per child    | Parent attrs live **once**, in `users`; child tables hold only their own attrs + a **shared `id`** (also used as FK) | Yes                         | No             | Yes                            |
| Table Per Class    | 1 parent (unused) + 1 per child     | Same as Mapped Superclass — parent's attrs **duplicated** into every child table, each with its own `id`; the extra parent table doesn't actually hold shared data | Yes (via UNION)            | No             | No (single class); UNION for hierarchy |
| Single Table       | 1 total (`users`)                   | **All attrs from every class merged into one row shape**, single shared `id`, plus a `userType` discriminator column | Yes                         | Yes (many)      | No                             |

**When to use which:**
- **Mapped Superclass** — child classes (`Mentor`, `TA`, `Instructor`) share no real DB-level relationship, just common Java fields.
- **Joined Table** — most commonly used in practice; best normalization, real polymorphic queries via the shared `users` table.
- **Table Per Class** — rarely used; same drawbacks as Mapped Superclass, just with an extra unused parent table.
- **Single Table** — best performance; fine when there aren't too many child-specific columns, since it avoids joins entirely at the cost of NULL-heavy rows.

---

## Polymorphic Queries — Quick Comparison

**Example query: "Get all Users" (regardless of subtype)**

| Strategy          | Can you write `SELECT u FROM User u`? | What actually runs under the hood       | Speed (fastest → slowest) |
|--------------------|------------------------------------------|-------------------------------------------|------------------------------|
| Mapped Superclass  | ❌ No — `User` isn't a real entity          | You must manually write/merge a `UNION` across `mentor`, `ta`, `instructor` yourself | 3rd (tied, slowest — plus manual effort) |
| Table Per Class    | ✅ Yes — `User` is a real entity            | Hibernate auto-generates the same kind of `UNION` across child tables for you | 3rd (tied, slowest — but no manual effort) |
| Joined Table       | ✅ Yes                                      | Hibernate runs `LEFT JOIN`s from `users` out to each child table | 2nd |
| Single Table       | ✅ Yes                                      | Simple `SELECT * FROM users` — one table, no join, no union | 1st (fastest) |

**Key takeaways:**
- **Single Table wins on speed** for polymorphic queries — no joins, no unions, just one table scan.
- **Joined Table** is the practical middle ground — normalized data, real polymorphic support, moderate join cost.
- **Mapped Superclass and Table Per Class perform identically** under the hood (both need a `UNION` across child tables) — the only difference is that Table Per Class lets JPA write and run that UNION for you, while Mapped Superclass leaves you to do it by hand.# Representing Inheritance in DB (JPA/Hibernate)

**What does "representing inheritance in DB" mean?**
In Java, classes can inherit from one another — a `Mentor`, `TA`, and `Instructor` can all extend a common `User` class, sharing fields like `id`, `name`, `email`, `password`, while also having their own unique fields. But relational databases don't have a native concept of "inheritance" — a table is just a flat set of columns. So when you use JPA/Hibernate to save these Java objects to a database, you have to pick a **strategy for how that class hierarchy gets translated into actual tables and columns**. That's what "representing inheritance in DB" refers to — the four strategies below are the different ways JPA lets you map an inheritance hierarchy onto relational tables.

**Extra note — what is a polymorphic query?**
A polymorphic query is any query written against the **parent type**, that transparently returns instances of **all its subclasses** combined — e.g., writing `SELECT u FROM User u` and getting back a mix of `Mentor`, `TA`, and `Instructor` objects, without needing to query each subclass separately. Whether this is possible (and how expensive it is) depends entirely on which inheritance mapping strategy is used below.

**Example used throughout:** a `User` parent class, with three child types — `Mentor`, `TA` (Teaching Assistant), and `Instructor`.

```
User (parent)
├── id
├── name
├── email
└── password

Mentor (child)          TA (child)              Instructor (child)
├── avgRating           ├── avgRating           ├── subject
└── company             └── noOfMR              └── noOfSessions
```

---

## 1. Mapped Superclass

**What it does:**
- Used when the parent class (`User`) **isn't a real entity** — it's just an **abstract** base holding shared fields.
- **No table is created for `User`.**
- **A table is created for each child class** (`mentor`, `ta`, `instructor`), and each one contains **all** attributes — both its own fields and everything inherited from `User`.
- Since there's no shared parent table, querying across all children requires a **UNION** of each child table.

**Resulting tables:**
```
mentor:     id | name | email | password | avgRating | company
ta:         id | name | email | password | noOfMR    | avgRating
instructor: id | name | email | password | subject   | noOfSessions
```

**Example query — "Get the email of all the users":**
```sql
SELECT email FROM tas
UNION
SELECT email FROM mentors
UNION
SELECT email FROM instructors;
```

**Code — Example 1 (User / Mentor / TA / Instructor):**
```java
@MappedSuperclass
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

**Code — Example 2 (generic `BaseModel`, common in real projects):**
```java
@Getter
@Setter
@MappedSuperclass
public abstract class BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt;
    private Date lastModifiedAt;
}

@Entity
public class Product extends BaseModel {
    private String title;
    private Double price;
    private String description;
}

@Entity
public class Category extends BaseModel {
    private String name;
}
```

Same idea as the `User` example — `BaseModel` isn't a real entity, `Product` and `Category` each get their own table, and both tables independently include `id`, `createdAt`, and `lastModifiedAt` alongside their own fields.

**Resulting tables (Example 2):**
```
product:  id | created_at | last_modified_at | title | price | description
category: id | created_at | last_modified_at | name
```

---

## 2. Joined Table *(most frequently used way)*

**What it does:**
- Creates **one table for the parent class** (`users`), and **one table for each child class**.
- The `users` table holds the shared fields (`id`, `name`, `email`, `password`).
- Each child table (`tas`, `mentors`, `instructor`) holds **only its own extra fields**, plus a `userId` column that's a **foreign key back to `users.id`**.
- To fetch a full child object, Hibernate performs a **JOIN** between `users` and that child's table.
- Most **normalized** option — no duplicate/wasted columns anywhere, and polymorphic queries work naturally off the `users` table.

**Resulting tables:**
```
users:      id | name | email | password
tas:        userId (FK -> users.id) | noOfMR | avgRating
mentors:    userId (FK -> users.id) | company | avgRating
instructor: userId (FK -> users.id) | subject | noOfSession
```

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

---

## 3. Table Per Class

**What it does:**
- **Exactly the same as Mapped Superclass** — the only difference is that here, we **also create a table for the parent class** (`users`), even though it isn't used to link the children together.
- Each child table (`mentor`, `ta`, `instructor`) is still **fully self-contained**, repeating the parent's columns.
- Polymorphic queries across children are possible via a **UNION**, same as Mapped Superclass — the standalone `users` table doesn't actually merge data from the children.

**Resulting tables:**
```
users:      id | name | email | password
mentor:     id | name | email | password | avgRating | company
ta:         id | name | email | password | noOfMR    | avgRating
instructor: id | name | email | password | subject   | noOfSessions
```

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

**Implementation gotcha (Hibernate):**
- `@GeneratedValue(strategy = GenerationType.IDENTITY)` is **not allowed** here — Hibernate
  throws `MappingException: Cannot use identity column key generation with <union-subclass>
  mapping`. Per-table auto-increment can't guarantee unique ids across sibling child tables
  once they're combined, so use `GenerationType.AUTO` (or `SEQUENCE`/`TABLE`) instead.
- The `users` table above only appears if `User` is a **concrete** class. If `User` is left
  `abstract`, Hibernate has nothing to instantiate for it and skips creating its table
  entirely — you'd get only the 3 child tables, same as Mapped Superclass.

---

## 4. Single Table

**What it does:**
- Creates **just ONE table** (`users`) with **all attributes from all classes** combined.
- Becomes a **sparse table** — lots of **NULLs**, since a `Mentor` row won't have a `subject` or `noOfSessions`, a `TA` row won't have `company`, etc.
- Uses a **`userType` discriminator column** to record which subclass each row actually represents:
  - `TA` → `1`
  - `Mentor` → `2`
  - `Instructor` → `3`
- **Fastest option** — no joins, no unions, everything lives in one table.

**Resulting table:**
```
users: id | name | email | password | noOfMR | avgRating | company | subject | noOfSessions | userType
```
(each row only fills in the columns relevant to its `userType`; everything else is NULL)

**Code:**
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "userType")
public abstract class User {
    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
}

@Entity
@DiscriminatorValue("2")
public class Mentor extends User {
    private Double avgRating;
    private String company;
}

@Entity
@DiscriminatorValue("1")
public class TA extends User {
    private Double avgRating;
    private Integer noOfMR;
}

@Entity
@DiscriminatorValue("3")
public class Instructor extends User {
    private String subject;
    private Integer noOfSessions;
}
```

---

## Quick Comparison

| Strategy          | # Tables                           | Attribute/key structure                                                   | Polymorphic queries      | Nulls?        | Joins needed?               |
|--------------------|-------------------------------------|-----------------------------------------------------------------------------|----------------------------|----------------|--------------------------------|
| Mapped Superclass  | 1 per child (no parent table)       | Parent's attrs **duplicated** into every child table, each with its own `id` | Not possible (UNION only)  | No             | No                             |
| Joined             | 1 parent (`users`) + 1 per child    | Parent attrs live **once**, in `users`; child tables hold only their own attrs + a **shared `id`** (also used as FK) | Yes                         | No             | Yes                            |
| Table Per Class    | 1 parent (unused) + 1 per child     | Same as Mapped Superclass — parent's attrs **duplicated** into every child table, each with its own `id`; the extra parent table doesn't actually hold shared data | Yes (via UNION)            | No             | No (single class); UNION for hierarchy |
| Single Table       | 1 total (`users`)                   | **All attrs from every class merged into one row shape**, single shared `id`, plus a `userType` discriminator column | Yes                         | Yes (many)      | No                             |

**When to use which:**
- **Mapped Superclass** — child classes (`Mentor`, `TA`, `Instructor`) share no real DB-level relationship, just common Java fields.
- **Joined Table** — most commonly used in practice; best normalization, real polymorphic queries via the shared `users` table.
- **Table Per Class** — rarely used; same drawbacks as Mapped Superclass, just with an extra unused parent table.
- **Single Table** — best performance; fine when there aren't too many child-specific columns, since it avoids joins entirely at the cost of NULL-heavy rows.

---

## Polymorphic Queries — Quick Comparison

**Example query: "Get all Users" (regardless of subtype)**

| Strategy          | Can you write `SELECT u FROM User u`? | What actually runs under the hood       | Speed (fastest → slowest) |
|--------------------|------------------------------------------|-------------------------------------------|------------------------------|
| Mapped Superclass  | ❌ No — `User` isn't a real entity          | You must manually write/merge a `UNION` across `mentor`, `ta`, `instructor` yourself | 3rd (tied, slowest — plus manual effort) |
| Table Per Class    | ✅ Yes — `User` is a real entity            | Hibernate auto-generates the same kind of `UNION` across child tables for you | 3rd (tied, slowest — but no manual effort) |
| Joined Table       | ✅ Yes                                      | Hibernate runs `LEFT JOIN`s from `users` out to each child table | 2nd |
| Single Table       | ✅ Yes                                      | Simple `SELECT * FROM users` — one table, no join, no union | 1st (fastest) |

**Key takeaways:**
- **Single Table wins on speed** for polymorphic queries — no joins, no unions, just one table scan.
- **Joined Table** is the practical middle ground — normalized data, real polymorphic support, moderate join cost.
- **Mapped Superclass and Table Per Class perform identically** under the hood (both need a `UNION` across child tables) — the only difference is that Table Per Class lets JPA write and run that UNION for you, while Mapped Superclass leaves you to do it by hand.