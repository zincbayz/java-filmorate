package ru.yandex.practicum.filmorate.util;

public enum FilmsMPARatings {

    G("G"), // — у фильма нет возрастных ограничений
    PG("PG"), // — детям рекомендуется смотреть фильм с родителями
    PG13("PG-13"), // — детям до 13 лет просмотр не желателен
    R("R"), // — лицам до 17 лет просматривать фильм можно только в присутствии взрослого
    NC17("NC-17"); // — лицам до 18 лет просмотр запрещён
    private String rating;

    FilmsMPARatings(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }
}
