package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.response.PaymentResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.PaymentMapper;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.PaymentRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.gateway.PaymentQrResult;
import org.example.order_app_backend.service.gateway.VietQrPaymentGateway;
import org.example.order_app_backend.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private VietQrPaymentGateway vietQrPaymentGateway;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private PaymentMapper paymentMapper = new PaymentMapper();

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private OrderEntity testOrder;

    @BeforeEach
    void setUp() {
        testOrder = OrderEntity.builder()
                .id(501L)
                .orderCode("KTX-20261102-0501")
                .userId(1L)
                .storeId(1L)
                .finalAmount(BigDecimal.valueOf(85000.00))
                .status(OrderStatus.PENDING)
                .paymentMethod(PaymentMethod.VIETQR)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
    }

    @Test
    @DisplayName("generatePaymentQr: Sinh mã VietQR chuẩn Napas với nội dung ORD_<ORDER_ID> thành công")
    void generatePaymentQr_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(testOrder));

        PaymentQrResult qrResult = PaymentQrResult.builder()
                .qrUrl("https://img.vietqr.io/image/970422-0987654321-compact2.png?amount=85000&addInfo=ORD_501")
                .vietqrContent("ORD_501")
                .bankBin("970422")
                .bankName("MBBank")
                .accountNumber("0987654321")
                .accountName("KTX FOOD ORDERING")
                .amount(BigDecimal.valueOf(85000.00))
                .build();

        when(vietQrPaymentGateway.generatePaymentQr(501L, "KTX-20261102-0501", BigDecimal.valueOf(85000.00)))
                .thenReturn(qrResult);

        when(paymentRepository.findByOrderId(501L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> {
            PaymentEntity p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PaymentResponse response = paymentService.generatePaymentQr(501L);

        assertNotNull(response);
        assertEquals(501L, response.getOrderId());
        assertEquals("ORD_501", response.getVietqrContent());
        assertEquals(PaymentMethod.VIETQR, response.getPaymentMethod());
        assertEquals(PaymentStatus.UNPAID, response.getStatus());
        assertTrue(response.getQrUrl().contains("ORD_501"));
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("generatePaymentQr: Ném lỗi ORDER_NOT_FOUND khi đơn hàng không tồn tại")
    void generatePaymentQr_OrderNotFound_ThrowsException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> paymentService.generatePaymentQr(999L));
        assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    @DisplayName("switchToCod: Kích hoạt dự phòng chuyển đổi sang COD thành công và đơn chuyển CONFIRMED")
    void switchToCod_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(501L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.switchToCod(501L);

        assertNotNull(response);
        assertEquals(PaymentMethod.COD, response.getPaymentMethod());
        assertEquals(PaymentStatus.UNPAID, response.getStatus());
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        assertEquals(PaymentMethod.COD, testOrder.getPaymentMethod());
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("switchToCod: Ném lỗi INVALID_ORDER_STATUS khi đơn hàng đã giao thành công DELIVERED")
    void switchToCod_DeliveredOrder_ThrowsException() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(testOrder));

        AppException ex = assertThrows(AppException.class, () -> paymentService.switchToCod(501L));
        assertEquals(ErrorCode.INVALID_ORDER_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("confirmPaymentSuccess: Xác nhận giao dịch thành công, cập nhật đơn hàng PAID và CONFIRMED")
    void confirmPaymentSuccess_Success() {
        when(orderRepository.findById(501L)).thenReturn(Optional.of(testOrder));
        when(paymentRepository.findByOrderId(501L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.confirmPaymentSuccess(501L, "TXN_MOCK_12345");

        assertNotNull(response);
        assertEquals(PaymentStatus.PAID, response.getStatus());
        assertEquals("TXN_MOCK_12345", response.getTransactionRef());
        assertEquals(PaymentStatus.PAID, testOrder.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }
}

