package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.user.Feed;

@Component
@Primary
@Slf4j
public class FeedRepositoryImpl implements FeedRepository{
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public FeedRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * создание компонента ленты
     */
    @Override
    public void createFeedEntity(int userId, int entityId, String eventType, String operation) {
        int idFeed = 1;
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("SELECT * FROM FEED ORDER BY EVENT_ID DESC LIMIT 1");
        if (userRows.next()) {
            idFeed = userRows.getInt("EVENT_ID");
            log.info("Последний установленный id: {}", idFeed);
            idFeed++;
        }
        log.info("Установлен id компонента ленты: {}", idFeed);

        Feed feed = new Feed(idFeed,userId,entityId,eventType,operation,System.currentTimeMillis());

        String sql = "INSERT INTO FEED(EVENT_ID, CREATE_TIME, USER_ID, OPERATION, EVENT_TYPE, ENTITY_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, idFeed, feed.getTimestamp(), userId, operation, eventType, entityId);
        log.info("Добавлен новый компонент ленты: {}", feed);
    }

}
