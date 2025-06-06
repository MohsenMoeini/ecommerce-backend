package com.ecommerce.service.interfaces;

import com.ecommerce.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Review review);
    Review updateReview(Long reviewId, Review review);
    Optional<Review> getReviewById(Long reviewId);
    List<Review> getReviewsByProductId(Long productId);
    Page<Review> getReviewsByProductId(Long productId, Pageable pageable);
    List<Review> getReviewsByUserId(Long userId);
    Double getAverageRatingForProduct(Long productId);
    void deleteReview(Long reviewId);
}
