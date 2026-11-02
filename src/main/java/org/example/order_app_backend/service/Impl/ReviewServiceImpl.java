package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateReviewRequest;
import org.example.order_app_backend.dto.response.ReviewResponse;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.entity.OrderStatus;
import org.example.order_app_backend.entity.ReviewEntity;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.ReviewMapper;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.ReviewRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        log.info("Bắt đầu xử lý đánh giá cho đơn hàng ID: {}", request.getOrderId());

        OrderEntity order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        validateOrderAccess(order);

        // BR-REV-01: Chỉ đánh giá khi đơn hàng đã hoàn tất giao nhận DELIVERED
        if (order.getStatus() != OrderStatus.DELIVERED) {
            log.warn("Đơn hàng ID {} chưa giao thành công, không thể đánh giá", order.getId());
            throw new AppException(ErrorCode.ORDER_NOT_DELIVERED);
        }

        // BR-REV-01: Mỗi đơn hàng chỉ được đánh giá 1 lần
        if (reviewRepository.existsByOrderId(order.getId())) {
            log.warn("Đơn hàng ID {} đã có đánh giá trước đó", order.getId());
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null) {
            currentUserId = order.getUserId();
        }

        ReviewEntity review = ReviewEntity.builder()
                .orderId(order.getId())
                .userId(currentUserId)
                .storeId(order.getStoreId())
                .storeRating(request.getStoreRating())
                .shipperRating(request.getShipperRating())
                .comment(request.getComment())
                .build();

        ReviewEntity savedReview = reviewRepository.save(review);

        // BR-REV-02: Tự động cập nhật rating_avg và rating_count của quán ăn
        StoreEntity store = storeRepository.findById(order.getStoreId()).orElse(null);
        String storeName = "Quán KTX";
        if (store != null) {
            storeName = store.getName();
            BigDecimal currentAvg = store.getRatingAvg() != null ? store.getRatingAvg() : BigDecimal.ZERO;
            int currentCount = store.getRatingCount() != null ? store.getRatingCount() : 0;

            BigDecimal totalScore = currentAvg.multiply(BigDecimal.valueOf(currentCount))
                    .add(BigDecimal.valueOf(request.getStoreRating()));
            int newCount = currentCount + 1;
            BigDecimal newAvg = totalScore.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

            store.setRatingAvg(newAvg);
            store.setRatingCount(newCount);
            storeRepository.save(store);
            log.info("Đã cập nhật rating quán ID {}: mới {} ({} lượt)", store.getId(), newAvg, newCount);
        }

        String userDisplayName = userRepository.findById(currentUserId)
                .map(UserEntity::getDisplayName)
                .orElse("Sinh viên KTX");

        log.info("Đánh giá thành công cho đơn hàng ID: {}, quán ID: {}", order.getId(), order.getStoreId());
        return reviewMapper.toResponse(savedReview, storeName, userDisplayName);
    }

    @Override
    public PageResponse<ReviewResponse> getStoreReviews(Long storeId, Pageable pageable) {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));

        Page<ReviewEntity> page = reviewRepository.findByStoreId(storeId, pageable);
        List<ReviewResponse> content = page.getContent().stream()
                .map(review -> {
                    String userDisplayName = userRepository.findById(review.getUserId())
                            .map(UserEntity::getDisplayName)
                            .orElse("Sinh viên KTX");
                    return reviewMapper.toResponse(review, store.getName(), userDisplayName);
                })
                .toList();

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }

    @Override
    public ReviewResponse getReviewByOrderId(Long orderId) {
        ReviewEntity review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        String storeName = storeRepository.findById(review.getStoreId())
                .map(StoreEntity::getName)
                .orElse("Quán KTX");

        String userDisplayName = userRepository.findById(review.getUserId())
                .map(UserEntity::getDisplayName)
                .orElse("Sinh viên KTX");

        return reviewMapper.toResponse(review, storeName, userDisplayName);
    }

    private void validateOrderAccess(OrderEntity order) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        boolean isAdmin = currentUserProvider.hasRole("ADMIN");

        if (currentUserId != null && !isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}

