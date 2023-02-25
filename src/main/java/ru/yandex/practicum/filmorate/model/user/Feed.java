package ru.yandex.practicum.filmorate.model.user;

import lombok.Data;

@Data
public class Feed {
    private long timestamp;
    private int userId;
    private String eventType;

    private String operation;

    private int eventId;

    private int entityId;
}
