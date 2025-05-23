CREATE TABLE IF NOT EXISTS ratings (
            id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            name VARCHAR(40) NOT NULL
          );
CREATE TABLE IF NOT EXISTS genres (
            id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            name VARCHAR(40) NOT NULL
          );
CREATE TABLE IF NOT EXISTS films (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            description VARCHAR(200),
            release_date DATE,
            duration INT,
            rating_id INT REFERENCES ratings(id)
          );
CREATE TABLE IF NOT EXISTS users (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            login VARCHAR(40) NOT NULL,
            email VARCHAR(255) NOT NULL,
            name VARCHAR(255) NOT NULL,
            birthday DATE
          );
CREATE TABLE IF NOT EXISTS film_genres (
            film_id BIGINT REFERENCES films(id) NOT NULL,
            genre_id INT REFERENCES genres(id) NOT NULL,
            PRIMARY KEY (film_id, genre_id)
          );
CREATE TABLE IF NOT EXISTS user_friends (
            user_id BIGINT REFERENCES users(id) NOT NULL,
            friend_id BIGINT REFERENCES users(id) NOT NULL,
            confirmed BOOLEAN NOT NULL,
            PRIMARY KEY (user_id, friend_id),
            CONSTRAINT check_not_self_friend CHECK (user_id != friend_id)
          );
CREATE TABLE IF NOT EXISTS likes (
            user_id BIGINT REFERENCES users(id) NOT NULL,
            film_id BIGINT REFERENCES films(id) NOT NULL,
            PRIMARY KEY (user_id, film_id)
          );