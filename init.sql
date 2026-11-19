CREATE TABLE app_user (
                          id BIGSERIAL PRIMARY KEY,
                          username VARCHAR(50),
                          password_hash VARCHAR(255),
                          settings_completed BOOLEAN NOT NULL DEFAULT FALSE,
                          number_of_people INTEGER,
                          weekly_budget NUMERIC(10, 2),
                          maximum_stores INTEGER ,
                          location_text VARCHAR(150),
                          latitude DOUBLE PRECISION,
                          longitude DOUBLE PRECISION,
                          radius_km DOUBLE PRECISION
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

CREATE TABLE shopping_plan_snapshot (
                                        id UUID PRIMARY KEY,

                                        user_id BIGINT NOT NULL
                                            REFERENCES app_user(id)
                                                ON DELETE CASCADE,

                                        created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        confirmed_at TIMESTAMPTZ,

                                        currency VARCHAR(3) NOT NULL DEFAULT 'NOK'
                                            CHECK (currency = 'NOK'),

                                        comparison_method VARCHAR(50) NOT NULL
                                                                        DEFAULT 'BEST_SINGLE_STORE_V1',

                                        baseline_total NUMERIC(12, 2),
                                        recommended_total NUMERIC(12, 2),

                                        comparison_note TEXT,

                                        snapshot_json TEXT NOT NULL,

                                        CONSTRAINT valid_comparison_totals CHECK (
                                            (
                                                baseline_total IS NULL
                                                    AND recommended_total IS NULL
                                                )
                                                OR
                                            (
                                                baseline_total IS NOT NULL
                                                    AND recommended_total IS NOT NULL
                                                    AND baseline_total >= 0
                                                    AND recommended_total >= 0
                                                    AND recommended_total <= baseline_total
                                                )
                                            ),

                                        CONSTRAINT valid_confirmation_date CHECK (
                                            confirmed_at IS NULL
                                                OR confirmed_at >= created_at
                                            )
);

CREATE INDEX idx_shopping_snapshot_user_confirmed
    ON shopping_plan_snapshot(user_id, confirmed_at);

INSERT INTO preference (code, display_name)
VALUES
    ('vegetarian', 'Vegetarian'),
    ('vegan', 'Vegan'),
    ('halal', 'Halal'),
    ('low_sugar', 'Low sugar'),
    ('kosher', 'Kosher');


INSERT INTO allergen (code, display_name)
VALUES
    ('egg', 'Egg'),
    ('fisk', 'Fish'),
    ('gluten', 'Gluten'),
    ('melk', 'Milk'),
    ('notter', 'Nuts'),
    ('peanotter', 'Peanuts'),
    ('soya', 'Soy');