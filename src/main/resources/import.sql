-- Training types (immutable, seeded once)
INSERT INTO training_type (training_type_name) VALUES
    ('Fitness'),
    ('Yoga'),
    ('Zumba'),
    ('Stretching'),
    ('Resistance');

-- Users: 3 trainees (id 1-3), 2 trainers (id 4-5)
-- NOTE: "user" is a reserved word in H2 and must be quoted
INSERT INTO "user" (first_name, last_name, username, password, is_active) VALUES
    ('Alice', 'Johnson',  'Alice.Johnson',  'xK9mP2qRnT', true),
    ('Bob',   'Williams', 'Bob.Williams',   'hL4vZ7wYcD', true),
    ('Carol', 'Davis',    'Carol.Davis',    'mQ8jF5tNbS', false),
    ('John',  'Smith',    'John.Smith',     'rW3eA6uXkG', true),
    ('Maria', 'Garcia',   'Maria.Garcia',   'nB1sY9pCfH', true);

INSERT INTO trainee (user_id, date_of_birth, address) VALUES
    (1, '1995-03-14', '12 Elm Street'),
    (2, '1990-07-22', '34 Oak Avenue'),
    (3, '1988-11-05', '78 Pine Road');

-- John specializes in Fitness (id 1), Maria in Yoga (id 2)
INSERT INTO trainer (user_id, specialization_id) VALUES
    (4, 1),
    (5, 2);

INSERT INTO training (trainee_id, trainer_id, training_name, training_type_id, training_date, training_duration) VALUES
    (1, 1, 'Morning Cardio', 1, '2025-01-10', 60),
    (2, 2, 'Upper Body',     2, '2025-01-12', 45),
    (1, 2, 'Full Body',      2, '2025-01-15', 90);
