package org.example.order_app_backend.service;

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
import org.example.order_app_backend.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private ReviewMapper reviewMapper = new ReviewMapper();

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private OrderEntity deliveredOrder;
    private StoreEntity testStore;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        deliveredOrder = OrderEntity.builder()
                .id(501L)
                .orderCode("KTX-20261102-0501")
                .userId(1L)
                .storeId(1L)
                .status(OrderStatus.DELIVERED)
                .build();

        testStore = StoreEntity.builder()
                .id(1L)
                .name("Cơm Tấm B2")
                .ratingAvg(BigDecimal.valueOf(4.00))
                .ratingCount(1)
                .build();

        testUser = UserEntity.builder()
                .id(1L)
                .displayName("Nguyễn Văn Sinh Viên")
                .build();
    }

    @Test
    @DisplayName("createReview: Đánh giá thành công và tự động tính lại rating_avg của quán ăn")
    void createReview_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(reviewRepository.existsByOrderId(501L)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(inv -> {
            ReviewEntity entity = inv.getArgument(0);
            entity.setId(801L);
            return entity;
        });
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(501L)
                .storeRating(5)
                .shipperRating(5)
                .comment("Cơm rất ngon, giao đúng giờ!")
                .build();

        ReviewResponse response = reviewService.createReview(request);

        assertNotNull(response);
        assertEquals(801L, response.getId());
        assertEquals(501L, response.getOrderId());
        assertEquals(1L, response.getStoreId());
        assertEquals("Cơm Tấm B2", response.getStoreName());
        assertEquals(5, response.getStoreRating());
        assertEquals(5, response.getShipperRating());
        assertEquals("Cơm rất ngon, giao đúng giờ!", response.getComment());

        // Kiểm tra tính toán lại điểm rating trung bình: (4.0 * 1 + 5) / 2 = 4.50
        assertEquals(new BigDecimal("4.50"), testStore.getRatingAvg());
        assertEquals(2, testStore.getRatingCount());
        verify(storeRepository).save(testStore);
    }

    @Test
    @DisplayName("createReview: Đơn chưa hoàn tất DELIVERED ném lỗi ORDER_NOT_DELIVERED")
    void createReview_OrderNotDelivered_ThrowsException() {
        deliveredOrder.setStatus(OrderStatus.DELIVERING);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(501L)
                .storeRating(5)
                .shipperRating(5)
                .build();

        AppException ex = assertThrows(AppException.class, () -> reviewService.createReview(request));
        assertEquals(ErrorCode.ORDER_NOT_DELIVERED, ex.getErrorCode());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("createReview: Đơn hàng đã đánh giá trước đó ném lỗi REVIEW_ALREADY_EXISTS")
    void createReview_Duplicate_ThrowsException() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(reviewRepository.existsByOrderId(501L)).thenReturn(true);

        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(501L)
                .storeRating(4)
                .shipperRating(4)
                .build();

        AppException ex = assertThrows(AppException.class, () -> reviewService.createReview(request));
        assertEquals(ErrorCode.REVIEW_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("getStoreReviews: Lấy danh sách đánh giá của quán có phân trang")
    void getStoreReviews_Success() {
        ReviewEntity entity = ReviewEntity.builder()
                .id(801L)
                .orderId(501L)
                .storeId(1L)
                .userId(1L)
                .storeRating(5)
                .shipperRating(5)
                .comment("Tuyệt vời")
                .build();

        Page<ReviewEntity> page = new PageImpl<>(List.of(entity));
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(reviewRepository.findByStoreId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        PageResponse<ReviewResponse> response = reviewService.getStoreReviews(1L, PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("Cơm Tấm B2", response.getContent().get(0).getStoreName());
        assertEquals("Nguyễn Văn Sinh Viên", response.getContent().get(0).getUserDisplayName());
    }
}

