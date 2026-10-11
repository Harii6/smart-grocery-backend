CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          number_of_people INTEGER NOT NULL,
                          weekly_budget NUMERIC(10, 2) NOT NULL,
                          maximum_stores INTEGER NOT NULL
);

CREATE TABLE preference (
                            id BIGSERIAL PRIMARY KEY,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            display_name VARCHAR(100) NOT NULL
);

CREATE TABLE allergen (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(50) NOT NULL UNIQUE,
                          display_name VARCHAR(100) NOT NULL
);

CREATE TABLE user_preference (
                                 user_id BIGINT NOT NULL,
                                 preference_id BIGINT NOT NULL,

                                 PRIMARY KEY (user_id, preference_id),

                                 CONSTRAINT fk_user_preference_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES app_user(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_user_preference_preference
                                     FOREIGN KEY (preference_id)
                                         REFERENCES preference(id)
                                         ON DELETE CASCADE
);

CREATE TABLE user_allergen (
                               user_id BIGINT NOT NULL,
                               allergen_id BIGINT NOT NULL,

                               PRIMARY KEY (user_id, allergen_id),

                               CONSTRAINT fk_user_allergen_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES app_user(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_user_allergen_allergen
                                   FOREIGN KEY (allergen_id)
                                       REFERENCES allergen(id)
                                       ON DELETE CASCADE
);

INSERT INTO preference (code, display_name)
VALUES
    ('vegetarian', 'Vegetarian'),
    ('vegan', 'Vegan'),
    ('halal', 'Halal'),
    ('low_sugar', 'Low sugar');

INSERT INTO allergen (code, display_name)
VALUES
    ('egg', 'Egg'),
    ('fisk', 'Fish'),
    ('gluten', 'Gluten'),
    ('melk', 'Milk'),
    ('notter', 'Nuts'),
    ('peanotter', 'Peanuts'),
    ('soya', 'Soy');