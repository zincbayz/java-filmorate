package ru.yandex.practicum.filmorate.model.film;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "Directors")
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "director_id")
    private int id;
    @Column(name = "director_name")
    private String name;

    public Director() {
    }
    public Director(int id, String name) {
        this.id = id;
        this.name = name;
    }
}