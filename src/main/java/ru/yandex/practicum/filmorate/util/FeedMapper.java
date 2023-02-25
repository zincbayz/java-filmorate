package ru.yandex.practicum.filmorate.util;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.user.Feed;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setTimestamp(rs.getTimestamp("created_at").getTime());
        feed.setUserId(rs.getInt("user_id"));
        feed.setEventType(rs.getString("event_type"));
        feed.setOperation(rs.getString("operation"));
        feed.setEventId(rs.getInt("event_id"));
        feed.setEntityId(rs.getInt("entity_id"));
        return feed;
    }
}
