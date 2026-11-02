package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.request.OrderItemRequest;
import org.example.order_app_backend.dto.request.SelectedItemOptionDto;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.entity.PaymentMethod;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.entity.UserRole;
import org.example.order_app_backend.repository.MenuItemRepository;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private StoreEntity testStore;
    private MenuItemEntity testMenuItem;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("student.order.test@fpt.edu.vn").orElseGet(() ->
                userRepository.save(UserEntity.builder()
                        .email("student.order.test@fpt.edu.vn")
                        .displayName("Sinh Viên Test Order")
                        .phone("0987654321")
                        .role(UserRole.STUDENT)
                        .firebaseUid("order_test_uid_001")
                        .build())
        );

        testStore = storeRepository.findAll().stream().findFirst().orElseGet(() ->
                storeRepository.save(StoreEntity.builder()
                        .name("Cơm Tấm B2 Test")
                        .description("Quán cơm test")
                        .address("Cổng B2")
                        .campusZone("KTX_KHU_B")
                        .isActive(true)
                        .ratingAvg(BigDecimal.valueOf(4.8))
                        .ratingCount(10)
                        .build())
        );

        testMenuItem = menuItemRepository.findByStoreId(testStore.getId()).stream().findFirst().orElseGet(() ->
                menuItemRepository.save(MenuItemEntity.builder()
                        .storeId(testStore.getId())
                        .name("Cơm Sườn Nướng Đặc Biệt")
                        .basePrice(BigDecimal.valueOf(35000.00))
                        .category("COM")
                        .isAvailable(true)
                        .build())
        );
    }

    @Test
    @DisplayName("API-006: POST /api/v1/orders - Tạo đơn hàng gom đơn KTX thành công (201 Created)")
    void createOrder_ShouldReturn201() throws Exception {
        SelectedItemOptionDto option = SelectedItemOptionDto.builder()
                .group("Kích cỡ")
                .choice("Lớn (+Cơm)")
                .extraPrice(BigDecimal.valueOf(5000.00))
                .build();

        OrderItemRequest item = OrderItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(2)
                .selectedOptions(List.of(option))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .paymentMethod(PaymentMethod.VIETQR)
                .note("Không hành lá")
                .items(List.of(item))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .header("X-Firebase-Uid", testUser.getFirebaseUid())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderCode", startsWith("KTX-")))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.paymentMethod").value("VIETQR"))
                .andExpect(jsonPath("$.data.paymentStatus").value("UNPAID"))
                .andExpect(jsonPath("$.data.deliveryFee").value(5000.00))
                .andExpect(jsonPath("$.data.pickupQrCode", startsWith("QR_KTX_")))
                .andExpect(jsonPath("$.data.pickupPin").isNotEmpty())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].itemName").value(testMenuItem.getName()))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(testMenuItem.getBasePrice().doubleValue()));
    }

    @Test
    @DisplayName("API-006: POST /api/v1/orders - Giỏ hàng rỗng trả về 400 Bad Request")
    void createOrder_EmptyItems_ShouldReturn400() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B")
                .paymentMethod(PaymentMethod.COD)
                .items(Collections.emptyList())
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("API-007: GET /api/v1/orders/{id} - Lấy chi tiết đơn hàng")
    void getOrderById_ShouldReturnOrderDetail() throws Exception {
        // Tạo đơn trước
        OrderItemRequest item = OrderItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(1)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_CHIEU_18H00")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .paymentMethod(PaymentMethod.COD)
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(responseBody).path("data").path("id").asLong();

        // Lấy chi tiết
        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.storeName").value(testStore.getName()))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("API-008: GET /api/v1/orders - Lấy lịch sử đơn hàng của người dùng")
    void getOrderHistory_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("API-009: POST /api/v1/orders/{id}/cancel - Hủy đơn hàng ở trạng thái PENDING thành công")
    void cancelOrder_PendingStatus_ShouldReturnCancelled() throws Exception {
        OrderItemRequest item = OrderItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(1)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Nhà xe Tòa BA1")
                .paymentMethod(PaymentMethod.COD)
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel")
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("API-010: PATCH /api/v1/orders/{id}/status - Cập nhật trạng thái đơn hàng sang PREPARING / DELIVERING")
    void updateOrderStatus_ShouldReturnUpdatedStatus() throws Exception {
        OrderItemRequest item = OrderItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(1)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .paymentMethod(PaymentMethod.COD)
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long orderId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        org.example.order_app_backend.dto.request.UpdateOrderStatusRequest statusReq =
                org.example.order_app_backend.dto.request.UpdateOrderStatusRequest.builder()
                        .status(org.example.order_app_backend.entity.OrderStatus.PREPARING)
                        .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/orders/" + orderId + "/status")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PREPARING"));
    }

    @Test
    @DisplayName("API-011: POST /api/v1/orders/{id}/confirm-pickup - Xác nhận nhận hàng thành công bằng mã PIN / QR")
    void confirmPickup_ShouldReturnDelivered() throws Exception {
        OrderItemRequest item = OrderItemRequest.builder()
                .menuItemId(testMenuItem.getId())
                .quantity(1)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(testStore.getId())
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .paymentMethod(PaymentMethod.COD)
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = createResult.getResponse().getContentAsString();
        long orderId = objectMapper.readTree(responseJson).path("data").path("id").asLong();
        String pickupPin = objectMapper.readTree(responseJson).path("data").path("pickupPin").asText();

        // Chuyển đơn sang DELIVERING
        org.example.order_app_backend.dto.request.UpdateOrderStatusRequest deliveringReq =
                org.example.order_app_backend.dto.request.UpdateOrderStatusRequest.builder()
                        .status(org.example.order_app_backend.entity.OrderStatus.DELIVERING)
                        .build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/orders/" + orderId + "/status")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deliveringReq)))
                .andExpect(status().isOk());

        // Xác nhận nhận hàng bằng mã PIN
        org.example.order_app_backend.dto.request.ConfirmPickupRequest confirmReq =
                org.example.order_app_backend.dto.request.ConfirmPickupRequest.builder()
                        .verificationMethod("PIN")
                        .code(pickupPin)
                        .build();

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/confirm-pickup")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.status").value("DELIVERED"))
                .andExpect(jsonPath("$.data.deliveredAt").isNotEmpty());
    }
}

