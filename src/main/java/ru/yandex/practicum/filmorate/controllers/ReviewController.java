package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public ReviewDto getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping()
    public List<ReviewDto> getAllReviewsByFilmId(@RequestParam(defaultValue = "0") String filmId,
                                                 @RequestParam(defaultValue = "10")String count) {
        List<ReviewDto> allDto = reviewService.getAllReviewsByFilmId(Integer.parseInt(filmId), Integer.parseInt(count));
        return allDto;
    }

    @PostMapping()
    public void addReview(@RequestBody ReviewDto reviewDto) {
        reviewService.addReview(reviewDto);
    }

    @PutMapping()
    public void editReview(@RequestBody ReviewDto reviewDto) {
        reviewService.editReview(reviewDto);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.deleteReview(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.rateReview(id, userId, 1, "like");
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislikeReview(@PathVariable int id, @PathVariable int userId) {
        reviewService.rateReview(id, userId, -1, "dislike");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteAssessment(id, userId, -1);    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteAssessment(id, userId, 1);
    }
}

