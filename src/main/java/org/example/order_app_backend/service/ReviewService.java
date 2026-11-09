package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateReviewRequest;
import org.example.order_app_backend.dto.response.ReviewResponse;
import org.example.order_app_backend.dto.response.ShipperProfileResponse;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    ReviewResponse createReview(CreateReviewRequest request);

    PageResponse<ReviewResponse> getStoreReviews(Long storeId, Pageable pageable);

    PageResponse<ReviewResponse> getShipperReviews(Long shipperId, Pageable pageable);

    ReviewResponse getReviewByOrderId(Long orderId);

    ShipperProfileResponse getShipperProfile(Long shipperId);
}
