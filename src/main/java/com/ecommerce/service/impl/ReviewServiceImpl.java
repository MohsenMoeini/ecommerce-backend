package com.ecommerce.service.impl;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ReviewRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.interfaces.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Review createReview(Review review) {
        // Validate product exists
        Product product = productRepository.findById(review.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        
        // Validate user exists
        User user = userRepository.findById(review.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        // Set references
        review.setProduct(product);
        review.setUser(user);
        
        // Set creation time if not already set
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        review.setUpdatedAt(LocalDateTime.now());
        
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review updateReview(Long reviewId, Review review) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        
        // Security check: ensure the user updating the review is the owner
        if (!existingReview.getUser().getId().equals(review.getUser().getId())) {
            throw new AccessDeniedException("You can only update your own reviews");
        }
        
        // Update fields
        existingReview.setRating(review.getRating());
        existingReview.setComment(review.getComment());
        existingReview.setUpdatedAt(LocalDateTime.now()); // Update modification time
        
        return reviewRepository.save(existingReview);
    }

    @Override
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return reviewRepository.findByProduct(product);
    }

    @Override
    public Page<Review> getReviewsByProductId(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return reviewRepository.findByProduct(product, pageable);
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return reviewRepository.findByUser(user);
    }

    @Override
    public Double getAverageRatingForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        return reviewRepository.findAverageRatingByProduct(product);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
    }
}
