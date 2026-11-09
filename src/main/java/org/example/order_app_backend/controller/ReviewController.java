package org.example.order_app_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.ApiResponse;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateReviewRequest;
import org.example.order_app_backend.dto.response.ReviewResponse;
import org.example.order_app_backend.dto.response.ShipperProfileResponse;
import org.example.order_app_backend.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Review Controller", description = "Các API đánh giá chất lượng quán ăn, shipper và xem danh sách đánh giá")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Đánh giá chất lượng Quán & Shipper (API-017)", description = "Chấm điểm 1-5 sao cho quán ăn và shipper kèm nhận xét sau khi nhận hàng thành công")
    public ApiResponse<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        log.info("Nhận yêu cầu đánh giá đơn hàng ID: {}", request.getOrderId());
        ReviewResponse response = reviewService.createReview(request);
        return ApiResponse.<ReviewResponse>builder()
                .success(true)
                .message("Đánh giá dịch vụ thành công")
                .data(response)
                .build();
    }

    @GetMapping("/stores/{storeId}/reviews")
    @Operation(summary = "Lấy danh sách đánh giá của quán ăn (API-018)", description = "Danh sách đánh giá của sinh viên theo quán ăn kèm phân trang")
    public ApiResponse<PageResponse<ReviewResponse>> getStoreReviews(
            @PathVariable Long storeId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Nhận yêu cầu lấy danh sách đánh giá cho quán ID: {}", storeId);
        PageResponse<ReviewResponse> response = reviewService.getStoreReviews(storeId, pageable);
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .success(true)
                .message("Lấy danh sách đánh giá thành công")
                .data(response)
                .build();
    }

    @GetMapping("/shippers/{shipperId}/reviews")
    @Operation(summary = "Lấy danh sách đánh giá của Shipper", description = "Danh sách đánh giá của sinh viên theo Shipper kèm phân trang")
    public ApiResponse<PageResponse<ReviewResponse>> getShipperReviews(
            @PathVariable Long shipperId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Nhận yêu cầu lấy danh sách đánh giá cho shipper ID: {}", shipperId);
        PageResponse<ReviewResponse> response = reviewService.getShipperReviews(shipperId, pageable);
        return ApiResponse.<PageResponse<ReviewResponse>>builder()
                .success(true)
                .message("Lấy danh sách đánh giá shipper thành công")
                .data(response)
                .build();
    }

    @GetMapping("/shippers/{shipperId}/profile")
    @Operation(summary = "Lấy hồ sơ và điểm đánh giá trung bình của Shipper", description = "Xem điểm sao trung bình, số lượt đánh giá, tổng số chuyến của shipper")
    public ApiResponse<ShipperProfileResponse> getShipperProfile(@PathVariable Long shipperId) {
        log.info("Lấy thông tin hồ sơ shipper ID: {}", shipperId);
        ShipperProfileResponse response = reviewService.getShipperProfile(shipperId);
        return ApiResponse.<ShipperProfileResponse>builder()
                .success(true)
                .message("Lấy hồ sơ shipper thành công")
                .data(response)
                .build();
    }

    @GetMapping("/orders/{orderId}/review")
    @Operation(summary = "Lấy thông tin đánh giá của đơn hàng", description = "Kiểm tra đơn hàng đã được đánh giá hay chưa và chi tiết đánh giá")
    public ApiResponse<ReviewResponse> getReviewByOrderId(@PathVariable Long orderId) {
        ReviewResponse response = reviewService.getReviewByOrderId(orderId);
        return ApiResponse.<ReviewResponse>builder()
                .success(true)
                .message("Lấy thông tin đánh giá thành công")
                .data(response)
                .build();
    }
}
