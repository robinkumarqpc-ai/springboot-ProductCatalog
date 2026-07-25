I want to create a package under my existing Models package called
RepresentingInheritanceInDB, to experiment with all 4 JPA inheritance
mapping strategies side by side. Location:

src/main/java/com/productservicing/productservice/Models/RepresentingInheritanceInDB/

Inside it, create 4 sub-packages, one per strategy:
- mappedsuperclass
- joined
- tableperclass
- singletable

Each sub-package should contain a User parent class and 3 child classes:
Mentor, TA, and Instructor — implementing that specific inheritance strategy.

Common fields:
- User (parent): id (Long), name (String), email (String), password (String)
- Mentor (child): avgRating (Double), company (String)
- TA (child): avgRating (Double), noOfMR (Integer)
- Instructor (child): subject (String), noOfSessions (Integer)

Since all 4 strategies use the same User/Mentor/TA/Instructor names, use this
naming convention to avoid class name and table name collisions:

1. mappedsuperclass package:
    - UserMappedSuperclass (@MappedSuperclass, abstract, no table)
    - MentorMappedSuperclass (@Entity, table name: mentor_mapped_superclass)
    - TAMappedSuperclass (@Entity, table name: ta_mapped_superclass)
    - InstructorMappedSuperclass (@Entity, table name: instructor_mapped_superclass)

2. joined package:
    - UserJoined (@Entity, @Inheritance(strategy = InheritanceType.JOINED),
      table name: users_joined)
    - MentorJoined (@Entity, table name: mentor_joined)
    - TAJoined (@Entity, table name: ta_joined)
    - InstructorJoined (@Entity, table name: instructor_joined)

3. tableperclass package:
    - UserTablePerClass (@Entity, @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS),
      table name: users_table_per_class)
    - MentorTablePerClass (@Entity, table name: mentor_table_per_class)
    - TATablePerClass (@Entity, table name: ta_table_per_class)
    - InstructorTablePerClass (@Entity, table name: instructor_table_per_class)

4. singletable package:
    - UserSingleTable (@Entity, @Inheritance(strategy = InheritanceType.SINGLE_TABLE),
      @DiscriminatorColumn(name = "user_type"), table name: users_single_table)
    - MentorSingleTable (@Entity, @DiscriminatorValue("MENTOR"))
    - TASingleTable (@Entity, @DiscriminatorValue("TA"))
    - InstructorSingleTable (@Entity, @DiscriminatorValue("INSTRUCTOR"))

Requirements:
- Use explicit @Table(name = "...") annotations on every @Entity so table
  names exactly match what I specified above (don't rely on default naming).
- Add @Id and @GeneratedValue(strategy = GenerationType.IDENTITY) on the id
  field wherever a real primary key is needed for that strategy.
- Add Lombok @Getter and @Setter on all classes.
- Make sure everything compiles cleanly and Spring Data JPA (already configured
  with spring.jpa.hibernate.ddl-auto=update and MySQL) will auto-create all
  these tables on startup.
- Don't touch or reference my existing Product/Category models — this is a
  completely separate, isolated experiment package.

Documentation:
- I already have a Docs package at
  src/main/java/com/productservicing/productservice/Docs/ containing a
  RepresentingInheritanceInDB.MD file with conceptual notes on these 4 strategies.
- After creating the code, add a short new section (or a separate file,
  RepresentingInheritanceInDB_Implementation.md, in that same Docs folder)
  that maps each package to the tables it creates — e.g. "mappedsuperclass
  package -> creates mentor_mapped_superclass, ta_mapped_superclass,
  instructor_mapped_superclass (no parent table)". Keep this documentation
  file separate from the actual @Entity Java classes — it's purely a
  reference note, not code.

After creating all files, run the app so Hibernate creates the tables, and
show me the final list of tables it created.