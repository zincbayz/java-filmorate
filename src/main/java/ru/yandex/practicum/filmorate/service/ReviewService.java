package ru.yandex.practicum.filmorate.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.ReviewNotFound;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FilmRepositoryImpl;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.UserRepositoryImpl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository repository;
    private final ModelMapper modelMapper;

    private final FilmRepositoryImpl filmRepository;

    private final UserRepositoryImpl userRepository;

    @Autowired
    public ReviewService(ReviewRepository repository, ModelMapper modelMapper, FilmRepositoryImpl filmRepository, UserRepositoryImpl userRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public ReviewDto addReview(ReviewDto reviewDto) {
        if(filmRepository.isFilmExist(reviewDto.getFilmId()) &&
                userRepository.isUserExist(reviewDto.getUserId())) {
            Review review = repository.save(convertDtoToReview(reviewDto));
            userRepository.insertFeed(review.getUserId(), "REVIEW", "ADD", review.getReviewId());
            return convertReviewToDto(review);
        } else {
            throw new RequiredObjectWasNotFound("User or Film not found");
        }
    }

    public ReviewDto updateReview(ReviewDto reviewDto) {
        isReviewExist(reviewDto.getReviewId());
        Review review = repository.findByReviewId(reviewDto.getReviewId());

        review.setContent(reviewDto.getContent());
        review.setIsPositive(reviewDto.getIsPositive());

        Review updatedReview = repository.save(review);
        userRepository.insertFeed(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId());
        return convertReviewToDto(updatedReview);
    }
    @Transactional
    public void deleteReview(int reviewId) {
        isReviewExist(reviewId);
        Review review = repository.findByReviewId(reviewId);
        userRepository.insertFeed(review.getUserId(), "REVIEW", "REMOVE", reviewId);
        repository.deleteById(reviewId);
    }

    public ReviewDto getReviewById(int reviewId) {
        isReviewExist(reviewId);
        return convertReviewToDto(repository.findByReviewId(reviewId));
    }

    public List<ReviewDto> getAllReviewsByFilmId(int filmId, int count) {
        List<Review> unsortedReviews;
        if(filmId == 0) {
            unsortedReviews = repository.findAll();
        } else {
            unsortedReviews = repository.findAllByFilmId(filmId);
        }
        return convertReviewsToDtoList(sortAllReviews(unsortedReviews, count));
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

    private List<Review> sortAllReviews(List<Review> unsortedReviews, int count) {
        return unsortedReviews.stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
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