--changeset Anjali:20250306
INSERT INTO app.part_unit (unit_name)
VALUES
    ('KG'),
    ('GM'),
    ('LTR'),
    ('NOS');
--rollback TRUNCATE app.part_unit CASCADE;