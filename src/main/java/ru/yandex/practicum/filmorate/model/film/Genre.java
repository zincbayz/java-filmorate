package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;

@Data
public class Genre {
    private int id;
    private String name;

    public Genre() {

    }
    public Genre(int genre_id, String name) {
        this.id= genre_id;
        this.name= name;
    }


}
