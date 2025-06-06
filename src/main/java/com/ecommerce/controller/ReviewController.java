package com.ecommerce.controller;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.Review;
import com.ecommerce.entity.User;
import com.ecommerce.service.interfaces.ProductService;
import com.ecommerce.service.interfaces.ReviewService;
import com.ecommerce.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviews() {
        List<Review> reviews = reviewService.getReviewsByProductId(null); // Get all reviews
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Review>> getReviewById(@PathVariable Long id) {
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        if (reviewOpt.isEmpty()) {
            ApiResponse<Review> errorResponse = ApiResponse.error("Review not found with id: " + id, HttpStatus.NOT_FOUND.value(), Review.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        return ResponseEntity.ok(ApiResponse.success(reviewOpt.get()));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByProductId(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/product/{productId}/paged")
    public ResponseEntity<ApiResponse<Page<Review>>> getPagedReviewsByProductId(
            @PathVariable Long productId,
            Pageable pageable) {
        Page<Review> reviews = reviewService.getReviewsByProductId(productId, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByUserId(@PathVariable Long userId) {
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRatingForProduct(@PathVariable Long productId) {
        Double averageRating = reviewService.getAverageRatingForProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(averageRating));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> createReview(@Valid @RequestBody Review review) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Review> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Review.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Optional<Product> productOpt = productService.getProductById(review.getProduct().getId());
        if (productOpt.isEmpty()) {
            ApiResponse<Review> errorResponse = ApiResponse.error("Product not found with id: " + review.getProduct().getId(), HttpStatus.NOT_FOUND.value(), Review.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        review.setUser(userOpt.get());
        review.setProduct(productOpt.get());
        
        Review createdReview = reviewService.createReview(review);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdReview, "Review created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody Review review) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            ApiResponse<Review> errorResponse = ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value(), Review.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        if (reviewOpt.isEmpty()) {
            ApiResponse<Review> errorResponse = ApiResponse.error("Review not found with id: " + id, HttpStatus.NOT_FOUND.value(), Review.class);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        Review existingReview = reviewOpt.get();
        User currentUser = userOpt.get();
        
        // Check if the review belongs to the current user or user is admin
        if (!existingReview.getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            ApiResponse<Review> errorResponse = ApiResponse.error("You don't have permission to update this review", HttpStatus.FORBIDDEN.value(), Review.class);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        review.setId(id);
        review.setUser(existingReview.getUser());
        review.setProduct(existingReview.getProduct());
        
        Review updatedReview = reviewService.updateReview(id, review);
        return ResponseEntity.ok(ApiResponse.success(updatedReview, "Review updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        Optional<User> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found", HttpStatus.NOT_FOUND.value()));
        }
        
        Optional<Review> reviewOpt = reviewService.getReviewById(id);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Review not found with id: " + id, HttpStatus.NOT_FOUND.value()));
        }
        
        Review review = reviewOpt.get();
        User currentUser = userOpt.get();
        
        // Check if the review belongs to the current user or user is admin
        if (!review.getUser().getId().equals(currentUser.getId()) && 
                !auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to delete this review", HttpStatus.FORBIDDEN.value()));
        }
        
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }
}
