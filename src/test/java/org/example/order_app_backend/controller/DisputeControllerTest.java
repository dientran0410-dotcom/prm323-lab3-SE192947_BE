package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.CreateDisputeRequest;
import org.example.order_app_backend.dto.request.ResolveDisputeRequest;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DisputeControllerTest {

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
    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("student.dispute.test@fpt.edu.vn").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("student.dispute.test@fpt.edu.vn")
                        .displayName("Sinh Viên Test Dispute")
                        .phone("0977112233")
                        .role(UserRole.STUDENT)
                        .firebaseUid("dispute_test_uid_001")
                        .build())
        );

        StoreEntity store = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(StoreEntity.builder()
                        .name("Cơm Tấm Khu B")
                        .campusZone("KTX_KHU_B")
                        .address("Cổng B2")
                        .isActive(true)
                        .build())
        );

        testOrder = orderRepository.save(OrderEntity.builder()
                .orderCode("KTX-DISP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId(testUser.getId())
                .storeId(store.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .subtotalAmount(BigDecimal.valueOf(40000.00))
                .deliveryFee(BigDecimal.valueOf(5000.00))
                .groupDiscountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(45000.00))
                .status(OrderStatus.DELIVERED)
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.PAID)
                .pickupQrCode("QR_DISP_001")
                .pickupPin("9988")
                .deliveredAt(Instant.now().minusSeconds(1800)) // 30 mins ago
                .build());
    }

    @Test
    @DisplayName("API-014: POST /api/v1/disputes - Gửi khiếu nại thành công (201 Created)")
    void createDispute_ShouldReturn201() throws Exception {
        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(testOrder.getId())
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Món ăn bị đổ vỡ khi nhận tại cổng")
                .evidenceImages(List.of("https://firebasestorage.googleapis.com/.../evidence1.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(30000.00))
                .build();

        mockMvc.perform(post("/api/v1/disputes")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.refundRequestedAmount").value(30000.00))
                .andExpect(jsonPath("$.data.evidenceImages", hasSize(1)));
    }

    @Test
    @DisplayName("API-014: POST /api/v1/disputes - Gửi khiếu nại thiếu ảnh bằng chứng trả về 400 Bad Request")
    void createDispute_EmptyEvidence_ShouldReturn400() throws Exception {
        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(testOrder.getId())
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Không có ảnh")
                .evidenceImages(Collections.emptyList())
                .refundRequestedAmount(BigDecimal.valueOf(20000.00))
                .build();

        mockMvc.perform(post("/api/v1/disputes")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-015: GET /api/v1/disputes/{id} - Lấy chi tiết khiếu nại")
    void getDisputeById_ShouldReturnDetail() throws Exception {
        // Tạo trước
        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(testOrder.getId())
                .reason(DisputeReason.WRONG_ITEM)
                .description("Giao nhầm món bún thay vì cơm")
                .evidenceImages(List.of("https://img.test/wrong.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(45000.00))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/disputes")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long disputeId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/disputes/" + disputeId)
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(disputeId))
                .andExpect(jsonPath("$.data.reason").value("WRONG_ITEM"));
    }

    @Test
    @DisplayName("API-016: GET /api/v1/disputes - Lấy danh sách lịch sử khiếu nại của sinh viên")
    void getMyDisputes_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/disputes")
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("API-015b: PATCH /api/v1/disputes/{id}/resolve - Thẩm định phê duyệt khiếu nại")
    void resolveDispute_ShouldUpdateStatus() throws Exception {
        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(testOrder.getId())
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Đổ canh")
                .evidenceImages(List.of("https://img.test/damaged.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(45000.00))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/disputes")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long disputeId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        ResolveDisputeRequest resolveReq = ResolveDisputeRequest.builder()
                .status(DisputeStatus.APPROVED)
                .refundApprovedAmount(BigDecimal.valueOf(45000.00))
                .resolutionNote("Đã xác minh và hoàn tiền")
                .build();

        mockMvc.perform(patch("/api/v1/disputes/" + disputeId + "/resolve")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.refundApprovedAmount").value(45000.00));
    }
}
