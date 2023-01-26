

CREATE TABLE IF NOT EXISTS Mpa (
    mpa_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    mpa_name varchar
);

CREATE TABLE IF NOT EXISTS Films (
    film_id INTEGER GENERATED BY DEFAULT AS IDENTITY Primary Key NOT NULL,
    name varchar,
    description varchar,
    releaseDate date,
    duration int,
    mpa_id INTEGER REFERENCES Mpa (mpa_id)
);


CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY Primary Key,
    email varchar,
    login varchar,
    name varchar,
    birthday date
);

CREATE TABLE IF NOT EXISTS Likes (
    film_id INTEGER REFERENCES Films (film_id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES Users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS Genres (
    genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY ,
    name varchar
);

CREATE TABLE IF NOT EXISTS Film_Genre (
    film_id INTEGER REFERENCES Films (film_id) ON DELETE CASCADE,
    genre_id INTEGER REFERENCES Genres (genre_id)
);

CREATE TABLE IF NOT EXISTS Friends (
    user_id INTEGER REFERENCES Users (user_id) ON DELETE CASCADE,
    friend_id INTEGER REFERENCES Users (user_id)
);

CREATE TABLE IF NOT EXISTS Invites (
    invite_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    from_id INTEGER REFERENCES Users (user_id) ON DELETE CASCADE,
    to_id INTEGER REFERENCES Users (user_id) ON DELETE CASCADE,
    status varchar
);

INSERT INTO Mpa (mpa_name) VALUES ('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
INSERT INTO Genres (name) VALUES ('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');