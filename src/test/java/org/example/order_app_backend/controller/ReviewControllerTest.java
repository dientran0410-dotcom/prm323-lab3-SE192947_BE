package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.CreateReviewRequest;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity testUser;
    private StoreEntity testStore;
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("student.review.test@fpt.edu.vn").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("student.review.test@fpt.edu.vn")
                        .displayName("Sinh Viên Test Review")
                        .phone("0966554433")
                        .role(UserRole.STUDENT)
                        .firebaseUid("review_test_uid_001")
                        .build())
        );

        testStore = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(StoreEntity.builder()
                        .name("Cơm Tấm B2 Review")
                        .campusZone("KTX_KHU_B")
                        .address("Cổng B2")
                        .ratingAvg(BigDecimal.valueOf(4.5))
                        .ratingCount(10)
                        .isActive(true)
                        .build())
        );

        testOrder = orderRepository.save(OrderEntity.builder()
                .orderCode("KTX-REV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId(testUser.getId())
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .subtotalAmount(BigDecimal.valueOf(50000.00))
                .deliveryFee(BigDecimal.valueOf(5000.00))
                .groupDiscountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(55000.00))
                .status(OrderStatus.DELIVERED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PAID)
                .pickupQrCode("QR_REV_001")
                .pickupPin("1122")
                .deliveredAt(Instant.now().minusSeconds(1000))
                .build());
    }

    @Test
    @DisplayName("API-017: POST /api/v1/reviews - Đánh giá dịch vụ thành công (201 Created)")
    void createReview_ShouldReturn201() throws Exception {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(testOrder.getId())
                .storeRating(5)
                .shipperRating(5)
                .comment("Đồ ăn rất ngon và nóng sốt")
                .build();

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.storeRating").value(5))
                .andExpect(jsonPath("$.data.shipperRating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Đồ ăn rất ngon và nóng sốt"));
    }

    @Test
    @DisplayName("API-017: POST /api/v1/reviews - Điểm đánh giá vượt quá 5 sao trả về 400 Bad Request")
    void createReview_InvalidRating_ShouldReturn400() throws Exception {
        CreateReviewRequest request = CreateReviewRequest.builder()
                .orderId(testOrder.getId())
                .storeRating(6) // Invalid rating
                .shipperRating(5)
                .build();

        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-018: GET /api/v1/stores/{storeId}/reviews - Lấy danh sách đánh giá của quán")
    void getStoreReviews_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/v1/stores/" + testStore.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}

