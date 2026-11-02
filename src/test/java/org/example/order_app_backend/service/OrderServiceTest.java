package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.request.OrderItemRequest;
import org.example.order_app_backend.dto.request.SelectedItemOptionDto;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.OrderMapper;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.OrderItemRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private StoreEntity testStore;
    private MenuItemEntity testItem;

    @BeforeEach
    void setUp() {
        testStore = StoreEntity.builder()
                .id(1L)
                .name("Cơm Tấm B2")
                .campusZone("KTX_KHU_B")
                .isActive(true)
                .build();

        testItem = MenuItemEntity.builder()
                .id(101L)
                .storeId(1L)
                .name("Cơm Sườn Nướng")
                .basePrice(BigDecimal.valueOf(35000.00))
                .isAvailable(true)
                .build();
    }

    @Test
    @DisplayName("createOrder: Tạo đơn thành công với Price Snapshot và phí ship gom đơn KTX")
    void createOrder_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(menuItemRepository.findByIdIn(List.of(101L))).thenReturn(List.of(testItem));

        SelectedItemOptionDto option = SelectedItemOptionDto.builder()
                .group("Kích cỡ")
                .choice("Lớn (+Cơm)")
                .extraPrice(BigDecimal.valueOf(5000.00))
                .build();

        OrderItemRequest itemReq = OrderItemRequest.builder()
                .menuItemId(101L)
                .quantity(2)
                .selectedOptions(List.of(option))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(1L)
                .slotTime("CA_TRUA_11H30")
                .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                .paymentMethod(PaymentMethod.VIETQR)
                .note("Không hành")
                .items(List.of(itemReq))
                .build();

        // unitPrice = 35000, extra = 5000, totalPerItem = 40000, qty = 2 -> subtotal = 80000
        // deliveryFee = 5000 (KTX slot), finalAmount = 85000
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> {
            OrderEntity order = inv.getArgument(0);
            order.setId(501L);
            return order;
        });

        when(orderItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(1L, response.getStoreId());
        assertEquals("Cơm Tấm B2", response.getStoreName());
        assertEquals(BigDecimal.valueOf(80000.00), response.getSubtotalAmount());
        assertEquals(BigDecimal.valueOf(5000.00), response.getDeliveryFee());
        assertEquals(BigDecimal.valueOf(85000.00), response.getFinalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(PaymentMethod.VIETQR, response.getPaymentMethod());
        assertEquals(PaymentStatus.UNPAID, response.getPaymentStatus());
        assertNotNull(response.getPickupQrCode());
        assertNotNull(response.getPickupPin());
        assertEquals(1, response.getItems().size());
        assertEquals("Cơm Sườn Nướng", response.getItems().get(0).getItemName());
        assertEquals(BigDecimal.valueOf(35000.00), response.getItems().get(0).getUnitPrice());
    }

    @Test
    @DisplayName("createOrder: Giỏ hàng rỗng ném lỗi ORDER_EMPTY")
    void createOrder_EmptyCart_ThrowsException() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(1L)
                .slotTime("CA_TRUA_11H30")
                .items(Collections.emptyList())
                .build();

        AppException ex = assertThrows(AppException.class, () -> orderService.createOrder(request));
        assertEquals(ErrorCode.ORDER_EMPTY, ex.getErrorCode());
    }

    @Test
    @DisplayName("createOrder: Quán đóng cửa ném lỗi STORE_INACTIVE")
    void createOrder_StoreInactive_ThrowsException() {
        testStore.setIsActive(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(1L)
                .slotTime("CA_TRUA_11H30")
                .items(List.of(OrderItemRequest.builder().menuItemId(101L).quantity(1).build()))
                .build();

        AppException ex = assertThrows(AppException.class, () -> orderService.createOrder(request));
        assertEquals(ErrorCode.STORE_INACTIVE, ex.getErrorCode());
    }

    @Test
    @DisplayName("createOrder: Món ăn hết hàng ném lỗi MENU_ITEM_UNAVAILABLE")
    void createOrder_ItemUnavailable_ThrowsException() {
        testItem.setIsAvailable(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(menuItemRepository.findByIdIn(List.of(101L))).thenReturn(List.of(testItem));

        CreateOrderRequest request = CreateOrderRequest.builder()
                .storeId(1L)
                .slotTime("CA_TRUA_11H30")
                .items(List.of(OrderItemRequest.builder().menuItemId(101L).quantity(1).build()))
                .build();

        AppException ex = assertThrows(AppException.class, () -> orderService.createOrder(request));
        assertEquals(ErrorCode.MENU_ITEM_UNAVAILABLE, ex.getErrorCode());
    }

    @Test
    @DisplayName("cancelOrder: Hủy đơn thành công khi trạng thái là PENDING")
    void cancelOrder_SuccessWhenPending() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .userId(1L)
                .storeId(1L)
                .status(OrderStatus.PENDING)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(501L)).thenReturn(Collections.emptyList());
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));

        OrderResponse response = orderService.cancelOrder(501L);

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("cancelOrder: Không cho phép hủy khi đơn đã PREPARING hoặc DELIVERING")
    void cancelOrder_FailsWhenNotPending() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .userId(1L)
                .storeId(1L)
                .status(OrderStatus.DELIVERING)
                .build();

        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        AppException ex = assertThrows(AppException.class, () -> orderService.cancelOrder(501L));
        assertEquals(ErrorCode.ORDER_CANNOT_BE_CANCELLED, ex.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmPickup: Đối soát mã QR thành công, chuyển đơn sang DELIVERED và ghi nhận deliveredAt")
    void confirmPickup_QrScan_Success() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .orderCode("KTX-20261102-0501")
                .status(OrderStatus.DELIVERING)
                .pickupQrCode("QR_HASH_AABBCC")
                .pickupPin("5678")
                .paymentMethod(PaymentMethod.VIETQR)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        org.example.order_app_backend.dto.request.ConfirmPickupRequest request =
                org.example.order_app_backend.dto.request.ConfirmPickupRequest.builder()
                        .verificationMethod("QR_SCAN")
                        .code("QR_HASH_AABBCC")
                        .build();

        org.example.order_app_backend.dto.response.PickupConfirmationResponse response = orderService.confirmPickup(501L, request);

        assertNotNull(response);
        assertEquals(501L, response.getOrderId());
        assertEquals(OrderStatus.DELIVERED, response.getStatus());
        assertNotNull(response.getDeliveredAt());
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
    }

    @Test
    @DisplayName("confirmPickup: Luồng thay thế bằng mã PIN thành công khi camera lỗi hoặc chói nắng")
    void confirmPickup_Pin_Success() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .orderCode("KTX-20261102-0501")
                .status(OrderStatus.DELIVERING)
                .pickupQrCode("QR_HASH_AABBCC")
                .pickupPin("5678")
                .paymentMethod(PaymentMethod.COD)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        org.example.order_app_backend.dto.request.ConfirmPickupRequest request =
                org.example.order_app_backend.dto.request.ConfirmPickupRequest.builder()
                        .verificationMethod("PIN")
                        .code("5678")
                        .build();

        org.example.order_app_backend.dto.response.PickupConfirmationResponse response = orderService.confirmPickup(501L, request);

        assertNotNull(response);
        assertEquals(OrderStatus.DELIVERED, response.getStatus());
        // Đơn COD nhận hàng sẽ tự động thanh toán PAID
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test
    @DisplayName("confirmPickup: Sai mã đối soát ném lỗi INVALID_PICKUP_CODE")
    void confirmPickup_InvalidCode_ThrowsException() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .status(OrderStatus.DELIVERING)
                .pickupQrCode("QR_HASH_AABBCC")
                .pickupPin("5678")
                .build();

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        org.example.order_app_backend.dto.request.ConfirmPickupRequest request =
                org.example.order_app_backend.dto.request.ConfirmPickupRequest.builder()
                        .verificationMethod("PIN")
                        .code("0000")
                        .build();

        AppException ex = assertThrows(AppException.class, () -> orderService.confirmPickup(501L, request));
        assertEquals(ErrorCode.INVALID_PICKUP_CODE, ex.getErrorCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmPickup: Đơn chưa sang DELIVERING ném lỗi INVALID_ORDER_STATUS")
    void confirmPickup_InvalidStatus_ThrowsException() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .status(OrderStatus.PENDING)
                .pickupQrCode("QR_HASH_AABBCC")
                .pickupPin("5678")
                .build();

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));

        org.example.order_app_backend.dto.request.ConfirmPickupRequest request =
                org.example.order_app_backend.dto.request.ConfirmPickupRequest.builder()
                        .verificationMethod("QR_SCAN")
                        .code("QR_HASH_AABBCC")
                        .build();

        AppException ex = assertThrows(AppException.class, () -> orderService.confirmPickup(501L, request));
        assertEquals(ErrorCode.INVALID_ORDER_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("updateOrderStatus: Cập nhật trạng thái đơn tiến trình hợp lệ")
    void updateOrderStatus_Success() {
        OrderEntity order = OrderEntity.builder()
                .id(501L)
                .status(OrderStatus.CONFIRMED)
                .storeId(1L)
                .build();

        when(orderRepository.findById(501L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(501L)).thenReturn(Collections.emptyList());
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));

        org.example.order_app_backend.dto.request.UpdateOrderStatusRequest request =
                org.example.order_app_backend.dto.request.UpdateOrderStatusRequest.builder()
                        .status(OrderStatus.PREPARING)
                        .build();

        OrderResponse response = orderService.updateOrderStatus(501L, request);

        assertNotNull(response);
        assertEquals(OrderStatus.PREPARING, response.getStatus());
        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }
}

