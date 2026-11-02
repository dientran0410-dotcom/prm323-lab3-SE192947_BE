package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.ConfirmPaymentRequest;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentControllerTest {

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
        testUser = userRepository.findByEmail("student.payment.test@fpt.edu.vn").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("student.payment.test@fpt.edu.vn")
                        .displayName("Sinh Viên Test Payment")
                        .phone("0912345678")
                        .role(UserRole.STUDENT)
                        .firebaseUid("payment_test_uid_001")
                        .build())
        );

        StoreEntity store = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(StoreEntity.builder()
                        .name("Quán Cơm KTX B")
                        .campusZone("KTX_KHU_B")
                        .address("Cổng B2")
                        .isActive(true)
                        .build())
        );

        testOrder = orderRepository.save(OrderEntity.builder()
                .orderCode("KTX-PAY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .userId(testUser.getId())
                .storeId(store.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .subtotalAmount(BigDecimal.valueOf(70000.00))
                .deliveryFee(BigDecimal.valueOf(5000.00))
                .groupDiscountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(75000.00))
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.VIETQR)
                .paymentStatus(PaymentStatus.UNPAID)
                .pickupQrCode("QR_PAY_TEST_001")
                .pickupPin("1234")
                .build());
    }

    @Test
    @DisplayName("API-012: POST /api/v1/payments/{orderId}/generate-qr - Sinh mã VietQR chuẩn Napas 247")
    void generatePaymentQr_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/payments/" + testOrder.getId() + "/generate-qr")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.vietqrContent").value("ORD_" + testOrder.getId()))
                .andExpect(jsonPath("$.data.paymentMethod").value("VIETQR"))
                .andExpect(jsonPath("$.data.status").value("UNPAID"))
                .andExpect(jsonPath("$.data.qrUrl", containsString("img.vietqr.io")))
                .andExpect(jsonPath("$.data.bankBin").value("970422"));
    }

    @Test
    @DisplayName("API-013: POST /api/v1/payments/{orderId}/switch-to-cod - Kích hoạt cơ chế dự phòng COD")
    void switchToCod_ShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/v1/payments/" + testOrder.getId() + "/switch-to-cod")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.data.status").value("UNPAID"));
    }

    @Test
    @DisplayName("API-012b: GET /api/v1/payments/{orderId} - Lấy thông tin thanh toán của đơn hàng")
    void getPaymentByOrderId_ShouldReturnPaymentInfo() throws Exception {
        // Sinh mã QR trước
        mockMvc.perform(post("/api/v1/payments/" + testOrder.getId() + "/generate-qr")
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk());

        // Lấy lại thông tin
        mockMvc.perform(get("/api/v1/payments/" + testOrder.getId())
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.paymentMethod").value("VIETQR"));
    }

    @Test
    @DisplayName("API-Payment-Confirm: POST /api/v1/payments/{orderId}/confirm-success - Xác nhận thanh toán")
    void confirmPaymentSuccess_ShouldUpdateStatusToPaid() throws Exception {
        ConfirmPaymentRequest request = ConfirmPaymentRequest.builder()
                .transactionRef("FT261102998877")
                .build();

        mockMvc.perform(post("/api/v1/payments/" + testOrder.getId() + "/confirm-success")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.transactionRef").value("FT261102998877"));
    }
}

