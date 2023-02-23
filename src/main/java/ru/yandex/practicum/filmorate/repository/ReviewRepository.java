package ru.yandex.practicum.filmorate.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.user.Feed;

import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Review findByReviewId(int reviewId);

    List<Review> findAllByFilmId(int filmId);

    @Modifying
    @Query(value = "insert into Review_User (review_id, user_id, assessment) values (?1, ?2, ?3)", nativeQuery = true)
    void rateReview(int reviewId, int userId, String assessment);

    @Modifying
    @Query(value = "delete from Review_User where review_id=?1 and user_id=?2", nativeQuery = true)
    void deleteAssessment(int reviewId, int userId);
}