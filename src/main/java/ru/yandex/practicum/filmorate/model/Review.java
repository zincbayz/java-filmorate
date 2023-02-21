package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private int reviewId;

    @Column(name = "content")
    private String content;

    @Column(name = "is_positive")
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "film_id")
    private Integer filmId;

    @Column(name = "useful")
    private int useful;

    public Review() {
    }
}