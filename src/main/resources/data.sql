INSERT INTO Mpa (mpa_name) VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
INSERT INTO Genres (name) VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');
INSERT INTO OPERATION(NAME)
VALUES ('Add'),
       ('Update'),
       ('Remove');

INSERT INTO EVENT_TYPE(NAME)
VALUES ('LIKE'),
       ('REVIEW'),
       ('FRIEND');

DELETE FROM EVENT_TYPE;
DELETE FROM OPERATION;
DELETE FROM FEED;

