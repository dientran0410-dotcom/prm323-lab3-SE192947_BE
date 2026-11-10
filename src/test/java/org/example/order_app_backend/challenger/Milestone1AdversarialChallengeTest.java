package org.example.order_app_backend.challenger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.dto.request.ConfirmPickupRequest;
import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.request.OrderItemRequest;
import org.example.order_app_backend.dto.response.CustomizationGroupDto;
import org.example.order_app_backend.dto.response.CustomizationOptionDto;
import org.example.order_app_backend.dto.response.PickupConfirmationResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.MenuItemMapper;
import org.example.order_app_backend.mapper.OrderMapper;
import org.example.order_app_backend.repository.*;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Milestone1AdversarialChallengeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MenuItemMapper menuItemMapper = new MenuItemMapper();

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    // =========================================================================
    // 1. ADVERSARIAL STRESS TEST: Jackson Customization Deserialization
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 1: Customization JSON Deserialization Edge Cases")
    class CustomizationDeserializationChallenge {

        @Test
        @DisplayName("Edge Case 1.1: Standard group/options syntax deserializes correctly")
        void standardGroupOptionsSyntax() throws Exception {
            String json = """
                    [
                      {
                        "group": "Kích cỡ",
                        "required": true,
                        "options": [
                          {"name": "Vừa", "extraPrice": 0},
                          {"name": "Lớn", "extraPrice": 5000}
                        ]
                      }
                    ]
                    """;
            List<CustomizationGroupDto> groups = objectMapper.readValue(json, new TypeReference<>() {});
            assertNotNull(groups);
            assertEquals(1, groups.size());
            assertEquals("Kích cỡ", groups.get(0).getGroup());
            assertTrue(groups.get(0).isRequired());
            assertEquals(2, groups.get(0).getOptions().size());
            assertEquals("Vừa", groups.get(0).getOptions().get(0).getName());
            assertEquals(BigDecimal.ZERO, groups.get(0).getOptions().get(0).getExtraPrice());
            assertEquals("Lớn", groups.get(0).getOptions().get(1).getName());
            assertEquals(new BigDecimal("5000"), groups.get(0).getOptions().get(1).getExtraPrice());
        }

        @Test
        @DisplayName("Edge Case 1.2: Seed Data syntax with 'name' and 'choices' alias deserializes correctly")
        void seedDataNameChoicesAliasSyntax() throws Exception {
            String json = """
                    [
                      {
                        "name": "Topping thêm",
                        "required": false,
                        "choices": [
                          {"name": "Trứng ốp la", "extraPrice": 5000},
                          {"option": "Chả trứng", "price": 6000},
                          {"choice": "Canh khổ qua", "extraPrice": 10000}
                        ]
                      }
                    ]
                    """;
            List<CustomizationGroupDto> groups = objectMapper.readValue(json, new TypeReference<>() {});
            assertNotNull(groups);
            assertEquals(1, groups.size());
            assertEquals("Topping thêm", groups.get(0).getGroup());
            assertFalse(groups.get(0).isRequired());
            assertEquals(3, groups.get(0).getOptions().size());
            assertEquals("Trứng ốp la", groups.get(0).getOptions().get(0).getName());
            assertEquals(new BigDecimal("5000"), groups.get(0).getOptions().get(0).getExtraPrice());
            assertEquals("Chả trứng", groups.get(0).getOptions().get(1).getName());
            assertEquals(new BigDecimal("6000"), groups.get(0).getOptions().get(1).getExtraPrice());
            assertEquals("Canh khổ qua", groups.get(0).getOptions().get(2).getName());
            assertEquals(new BigDecimal("10000"), groups.get(0).getOptions().get(2).getExtraPrice());
        }

        @Test
        @DisplayName("Edge Case 1.3: Empty array, blank string, and null return empty collection safely in MenuItemMapper")
        void emptyAndNullInputsHandledGracefully() {
            MenuItemEntity entityNull = MenuItemEntity.builder().customizationOptions(null).build();
            assertEquals(0, menuItemMapper.toResponse(entityNull).getCustomizationOptions().size());

            MenuItemEntity entityBlank = MenuItemEntity.builder().customizationOptions("   ").build();
            assertEquals(0, menuItemMapper.toResponse(entityBlank).getCustomizationOptions().size());

            MenuItemEntity entityEmptyArray = MenuItemEntity.builder().customizationOptions("[]").build();
            assertEquals(0, menuItemMapper.toResponse(entityEmptyArray).getCustomizationOptions().size());
        }

        @Test
        @DisplayName("Edge Case 1.4: Unknown attributes do not break deserialization due to @JsonIgnoreProperties")
        void unknownAttributesIgnoredGracefully() throws Exception {
            String jsonWithExtraFields = """
                    [
                      {
                        "group": "Mức đường",
                        "required": true,
                        "legacyId": 9999,
                        "uiWidgetType": "RADIO_BUTTON",
                        "options": [
                          {"name": "50%", "extraPrice": 0, "isDefault": true, "barcode": "XYZ-123"}
                        ]
                      }
                    ]
                    """;
            List<CustomizationGroupDto> groups = objectMapper.readValue(jsonWithExtraFields, new TypeReference<>() {});
            assertNotNull(groups);
            assertEquals(1, groups.size());
            assertEquals("Mức đường", groups.get(0).getGroup());
            assertEquals(1, groups.get(0).getOptions().size());
            assertEquals("50%", groups.get(0).getOptions().get(0).getName());
        }

        @Test
        @DisplayName("Edge Case 1.5: Null fields within group and options deserialize without crashing")
        void nullFieldsWithinElementsHandled() throws Exception {
            String jsonWithNulls = """
                    [
                      {
                        "group": null,
                        "required": false,
                        "options": [
                          {"name": null, "extraPrice": null}
                        ]
                      }
                    ]
                    """;
            List<CustomizationGroupDto> groups = objectMapper.readValue(jsonWithNulls, new TypeReference<>() {});
            assertNotNull(groups);
            assertEquals(1, groups.size());
            assertNull(groups.get(0).getGroup());
            assertEquals(1, groups.get(0).getOptions().size());
            assertNull(groups.get(0).getOptions().get(0).getName());
            assertNull(groups.get(0).getOptions().get(0).getExtraPrice());
        }

        @Test
        @DisplayName("Edge Case 1.6: Malformed JSON recovers gracefully to empty list without crashing mapper")
        void malformedJsonFallsBackToEmptyList() {
            MenuItemEntity corruptedEntity = MenuItemEntity.builder()
                    .customizationOptions("{malformed: [json, broken")
                    .build();

            assertDoesNotThrow(() -> {
                var response = menuItemMapper.toResponse(corruptedEntity);
                assertNotNull(response.getCustomizationOptions());
                assertTrue(response.getCustomizationOptions().isEmpty());
            });
        }
    }

    // =========================================================================
    // 2. ADVERSARIAL STRESS TEST: OrderServiceImpl.confirmPickup Status Enforcement
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 2: OrderServiceImpl.confirmPickup Status Enforcement")
    class ConfirmPickupStatusChallenge {

        @ParameterizedTest(name = "Non-DELIVERING status {0} must throw AppException(INVALID_ORDER_STATUS)")
        @EnumSource(value = OrderStatus.class, names = {"PENDING", "CONFIRMED", "PREPARING", "READY", "DELIVERED", "CANCELLED"})
        void allNonDeliveringStatuses_ThrowInvalidOrderStatus(OrderStatus status) {
            OrderEntity order = OrderEntity.builder()
                    .id(777L)
                    .status(status)
                    .pickupQrCode("QR_VALID_123")
                    .pickupPin("1234")
                    .build();

            when(orderRepository.findById(777L)).thenReturn(Optional.of(order));

            ConfirmPickupRequest request = ConfirmPickupRequest.builder()
                    .verificationMethod("QR_SCAN")
                    .code("QR_VALID_123")
                    .build();

            AppException ex = assertThrows(AppException.class, () -> orderService.confirmPickup(777L, request));
            assertEquals(ErrorCode.INVALID_ORDER_STATUS, ex.getErrorCode());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Status null also safely throws AppException(INVALID_ORDER_STATUS)")
        void nullStatus_ThrowsInvalidOrderStatus() {
            OrderEntity order = OrderEntity.builder()
                    .id(777L)
                    .status(null)
                    .pickupQrCode("QR_VALID_123")
                    .pickupPin("1234")
                    .build();

            when(orderRepository.findById(777L)).thenReturn(Optional.of(order));

            ConfirmPickupRequest request = ConfirmPickupRequest.builder()
                    .verificationMethod("PIN")
                    .code("1234")
                    .build();

            AppException ex = assertThrows(AppException.class, () -> orderService.confirmPickup(777L, request));
            assertEquals(ErrorCode.INVALID_ORDER_STATUS, ex.getErrorCode());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("DELIVERING order succeeds with valid QR and updates COD payment to PAID")
        void deliveringOrder_ValidQR_SucceedsAndMarksCodPaid() {
            OrderEntity order = OrderEntity.builder()
                    .id(777L)
                    .status(OrderStatus.DELIVERING)
                    .pickupQrCode("QR_VALID_123")
                    .pickupPin("1234")
                    .paymentMethod(PaymentMethod.COD)
                    .paymentStatus(PaymentStatus.UNPAID)
                    .build();

            when(orderRepository.findById(777L)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            ConfirmPickupRequest request = ConfirmPickupRequest.builder()
                    .verificationMethod("QR_SCAN")
                    .code("QR_VALID_123")
                    .build();

            PickupConfirmationResponse resp = orderService.confirmPickup(777L, request);

            assertNotNull(resp);
            assertEquals(OrderStatus.DELIVERED, resp.getStatus());
            assertNotNull(resp.getDeliveredAt());
            assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("DELIVERING order with invalid code throws INVALID_PICKUP_CODE")
        void deliveringOrder_InvalidCode_ThrowsInvalidPickupCode() {
            OrderEntity order = OrderEntity.builder()
                    .id(777L)
                    .status(OrderStatus.DELIVERING)
                    .pickupQrCode("QR_VALID_123")
                    .pickupPin("1234")
                    .build();

            when(orderRepository.findById(777L)).thenReturn(Optional.of(order));

            ConfirmPickupRequest request = ConfirmPickupRequest.builder()
                    .verificationMethod("PIN")
                    .code("9999")
                    .build();

            AppException ex = assertThrows(AppException.class, () -> orderService.confirmPickup(777L, request));
            assertEquals(ErrorCode.INVALID_PICKUP_CODE, ex.getErrorCode());
            verify(orderRepository, never()).save(any());
        }
    }

    // =========================================================================
    // 3. ADVERSARIAL STRESS TEST: Mock Robustness & User Ban Check
    // =========================================================================
    @Nested
    @DisplayName("Challenge Area 3: OrderService Mock Robustness & Security Checks")
    class MockRobustnessChallenge {

        @Test
        @DisplayName("createOrder: User not found in repository throws USER_NOT_FOUND (not NPE)")
        void createOrder_UserNotFound_ThrowsException() {
            when(currentUserProvider.getCurrentUserId()).thenReturn(999L);
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            StoreEntity store = StoreEntity.builder().id(1L).isActive(true).build();
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            MenuItemEntity item = MenuItemEntity.builder().id(101L).storeId(1L).basePrice(BigDecimal.TEN).isAvailable(true).build();
            when(menuItemRepository.findByIdIn(List.of(101L))).thenReturn(List.of(item));

            CreateOrderRequest req = CreateOrderRequest.builder()
                    .storeId(1L)
                    .items(List.of(OrderItemRequest.builder().menuItemId(101L).quantity(1).build()))
                    .build();

            AppException ex = assertThrows(AppException.class, () -> orderService.createOrder(req));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
            verify(userRepository).findById(999L);
        }

        @Test
        @DisplayName("createOrder: Banned user throws USER_BANNED (not NPE)")
        void createOrder_BannedUser_ThrowsException() {
            UserEntity bannedUser = UserEntity.builder().id(2L).isBanned(true).build();

            when(currentUserProvider.getCurrentUserId()).thenReturn(2L);
            when(userRepository.findById(2L)).thenReturn(Optional.of(bannedUser));

            StoreEntity store = StoreEntity.builder().id(1L).isActive(true).build();
            when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

            MenuItemEntity item = MenuItemEntity.builder().id(101L).storeId(1L).basePrice(BigDecimal.TEN).isAvailable(true).build();
            when(menuItemRepository.findByIdIn(List.of(101L))).thenReturn(List.of(item));

            CreateOrderRequest req = CreateOrderRequest.builder()
                    .storeId(1L)
                    .items(List.of(OrderItemRequest.builder().menuItemId(101L).quantity(1).build()))
                    .build();

            AppException ex = assertThrows(AppException.class, () -> orderService.createOrder(req));
            assertEquals(ErrorCode.USER_BANNED, ex.getErrorCode());
            verify(userRepository).findById(2L);
        }
    }
}
