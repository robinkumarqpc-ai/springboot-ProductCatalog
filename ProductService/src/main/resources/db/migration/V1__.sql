CREATE TABLE categories
(
    created_at       datetime NULL,
    id               BIGINT AUTO_INCREMENT NOT NULL,
    last_modified_at datetime NULL,
    name             VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE instructor_joined
(
    no_of_sessions INT NULL,
    id             BIGINT NOT NULL,
    subject        VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE instructor_mapped_superclass
(
    no_of_sessions INT NULL,
    id             BIGINT AUTO_INCREMENT NOT NULL,
    email          VARCHAR(255) NULL,
    name           VARCHAR(255) NULL,
    password       VARCHAR(255) NULL,
    subject        VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE instructor_table_per_class
(
    no_of_sessions INT NULL,
    id             BIGINT NOT NULL,
    email          VARCHAR(255) NULL,
    name           VARCHAR(255) NULL,
    password       VARCHAR(255) NULL,
    subject        VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE mentor_joined
(
    avg_rating DOUBLE NULL,
    id      BIGINT NOT NULL,
    company VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE mentor_mapped_superclass
(
    avg_rating DOUBLE NULL,
    id       BIGINT AUTO_INCREMENT NOT NULL,
    company  VARCHAR(255) NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE mentor_table_per_class
(
    avg_rating DOUBLE NULL,
    id       BIGINT NOT NULL,
    company  VARCHAR(255) NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE product
(
    price DOUBLE NULL,
    category_id      BIGINT NULL,
    created_at       datetime NULL,
    id               BIGINT AUTO_INCREMENT NOT NULL,
    last_modified_at datetime NULL,
    `description`    VARCHAR(255) NULL,
    imgurl           VARCHAR(255) NULL,
    title            VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE ta_joined
(
    avg_rating DOUBLE NULL,
    no_ofmr INT NULL,
    id      BIGINT NOT NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE ta_mapped_superclass
(
    avg_rating DOUBLE NULL,
    no_ofmr  INT NULL,
    id       BIGINT AUTO_INCREMENT NOT NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE ta_table_per_class
(
    avg_rating DOUBLE NULL,
    no_ofmr  INT NULL,
    id       BIGINT NOT NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE users_joined
(
    id       BIGINT AUTO_INCREMENT NOT NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE users_single_table
(
    avg_rating DOUBLE NULL,
    no_of_sessions INT NULL,
    no_ofmr        INT NULL,
    id             BIGINT AUTO_INCREMENT NOT NULL,
    user_type      VARCHAR(31) NOT NULL,
    company        VARCHAR(255) NULL,
    email          VARCHAR(255) NULL,
    name           VARCHAR(255) NULL,
    password       VARCHAR(255) NULL,
    subject        VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE users_table_per_class
(
    id       BIGINT NOT NULL,
    email    VARCHAR(255) NULL,
    name     VARCHAR(255) NULL,
    password VARCHAR(255) NULL,
    CONSTRAINT `PRIMARY` PRIMARY KEY (id)
);

CREATE TABLE users_table_per_class_seq
(
    next_val BIGINT NULL
);

ALTER TABLE ta_joined
    ADD CONSTRAINT FK2kda0p3mij7237plt0mv14smw FOREIGN KEY (id) REFERENCES users_joined (id) ON DELETE NO ACTION;

ALTER TABLE mentor_joined
    ADD CONSTRAINT FKgnbpvmnms7n5ohkehs6weu930 FOREIGN KEY (id) REFERENCES users_joined (id) ON DELETE NO ACTION;

ALTER TABLE instructor_joined
    ADD CONSTRAINT FKo7k4skvqpeqhh487yy7k3iiw5 FOREIGN KEY (id) REFERENCES users_joined (id) ON DELETE NO ACTION;

ALTER TABLE product
    ADD CONSTRAINT FKowomku74u72o6h8q0khj7id8q FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE NO ACTION;

CREATE INDEX FKowomku74u72o6h8q0khj7id8q ON product (category_id);