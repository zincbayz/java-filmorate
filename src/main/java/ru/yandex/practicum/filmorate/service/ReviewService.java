package ru.yandex.practicum.filmorate.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.ReviewNotFound;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository repository;
    private final ModelMapper modelMapper;

    @Autowired
    public ReviewService(ReviewRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public void addReview(ReviewDto reviewDto) {
        repository.save(convertDtoToReview(reviewDto));
    }

    public void editReview(ReviewDto reviewDto) {
        Review review = convertDtoToReview(reviewDto);
        isReviewExist(review.getReviewId());
        repository.save(review);
    }

    public void deleteReview(int reviewId) {
        isReviewExist(reviewId);
        repository.deleteById(reviewId);
    }

    public ReviewDto getReviewById(int reviewId) {
        isReviewExist(reviewId);
        return convertReviewToDto(repository.findByReviewId(reviewId));
    }

    public List<ReviewDto> getAllReviewsByFilmId(int filmId, int count) {
        if(filmId == 0) {
            return convertReviewsToDtoList(repository.findAll());
        } else {
            List<Review> reviews = repository.findByFilmIdEquals(filmId).stream()
                    .sorted(Comparator.comparing(Review::getUseful).reversed())
                    .limit(count)
                    .collect(Collectors.toList());
            return convertReviewsToDtoList(reviews);
        }
    }
    @Transactional
    public void rateReview(int reviewId, int userId, int usefulness, String assessment) {
        isReviewExist(reviewId);
        setReviewUsefulness(reviewId, usefulness);
        repository.rateReview(reviewId, userId, assessment);
    }
    @Transactional
    public void deleteAssessment(int reviewId, int userId, int usefulness) {
        isReviewExist(reviewId);
        setReviewUsefulness(reviewId, usefulness);
        repository.deleteAssessment(reviewId, userId);
    }

    private void setReviewUsefulness(int reviewId, int usefulness) {
        Review review = repository.findByReviewId(reviewId);
        review.setUseful(review.getUseful() + usefulness);
        repository.save(review);
    }

    private void isReviewExist(int reviewId) {
        if(!repository.existsById(reviewId)) {
            throw new ReviewNotFound("Review id: " + reviewId);
        }
    }

    private Review convertDtoToReview(ReviewDto reviewDto) {
        return this.modelMapper.map(reviewDto, Review.class);

    }

    private ReviewDto convertReviewToDto(Review review) {
        return this.modelMapper.map(review, ReviewDto.class);
    }

    private List<ReviewDto> convertReviewsToDtoList(List<Review> reviews) {
        return reviews.stream()
                .map(review -> this.modelMapper.map(review, ReviewDto.class))
                .collect(Collectors.toList());
    }
}
