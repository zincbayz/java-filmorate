package ru.yandex.practicum.filmorate.repository;

public interface FeedRepository {
    /**
     * создание компонента ленты
     */
    static void createFeedEntity(int userId, int entityId, String eventType, String operation);
}
