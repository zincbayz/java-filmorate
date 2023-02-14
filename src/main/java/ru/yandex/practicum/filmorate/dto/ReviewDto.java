package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@Data
public class ReviewDto {
    private int reviewId;
    private String content;
    @JsonProperty("isPositive")
    private boolean isPositive;
    private int userId;
    private int filmId;

    private int useful;


}
