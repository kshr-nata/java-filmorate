# java-filmorate
Template repository for Filmorate project.

![ER-Диаграмма](./DBDiagram.png)

## Описание таблиц

### User
Таблица содержит информацию о пользователях приложения:
- `user_id`: Уникальный идентификатор пользователя.
- `email`: Электронная почта пользователя.
- `login`: Логин пользователя.
- `name`: Имя пользователя (если не указано, используется логин).
- `birthday`: Дата рождения пользователя.

### Film
Таблица содержит информацию о фильмах:
- `film_id`: Уникальный идентификатор фильма.
- `name`: Название фильма.
- `description`: Описание фильма (до 200 символов).
- `release_date`: Дата выхода фильма.
- `duration`: Продолжительность фильма (в минутах).
- `rating`: Строка, одно из предопределенных значений (R, NC_17, PG_13, G, PG)


### Genre
Таблица содержит жанры фильмов:
- `genre_id`: Уникальный идентификатор жанра.
- `name`: Название жанра (например, Комедия, Драма).

### user_friends
Таблица описывает связи между пользователями (дружбу):
- `id`: Уникальный идентификатор записи о дружбе
- `user_id`: Идентификатор пользователя.
- `friend_id`: Идентификатор друга.
- `confirmed`: boolean, отметка о том, что дружба подтверждена

### likes
Таблица описывает лайки, которые пользователи ставят фильмам:
- `like_id`: Уникальный идентификатор записи о лайке.
- `user_id`: Идентификатор пользователя.
- `film_id`: Идентификатор фильма.

## Примеры запросов

### 10 самых популярных фильмов
 ```SELECT 
    f.film_id,
    f.name AS film_name,
    COUNT(l.userId) AS likes_count
FROM 
    film f
LEFT JOIN 
    likes l ON f.film_id = l.filmId
GROUP BY 
    f.film_id, f.name
ORDER BY 
    likes_count DESC
LIMIT 10;

### Общие с пользователем друзья
-- Найти общих друзей между user1 (например, ID=1) и user2 (например, ID=2)
 ```SELECT 
    u.user_id,
    u.login,
    u.name,
    u.email
FROM 
    user_friends uf1
JOIN 
    user_friends uf2 ON uf1.friend_id = uf2.friend_id
JOIN 
    user u ON uf1.friend_id = u.user_id
WHERE 
    uf1.user_id = 1  -- ID первого пользователя
    AND uf2.user_id = 2  -- ID второго пользователя
    AND uf1.confirmed = TRUE  -- Только подтвержденные друзья (если важно)
    AND uf2.confirmed = TRUE;
