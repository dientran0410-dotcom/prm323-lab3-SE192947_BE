package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.CreateDisputeRequest;
import org.example.order_app_backend.dto.request.ResolveDisputeRequest;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.DisputeMapper;
import org.example.order_app_backend.repository.DisputeRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.impl.DisputeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepository disputeRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Spy
    private DisputeMapper disputeMapper = new DisputeMapper();

    @InjectMocks
    private DisputeServiceImpl disputeService;

    private OrderEntity deliveredOrder;

    @BeforeEach
    void setUp() {
        deliveredOrder = OrderEntity.builder()
                .id(501L)
                .orderCode("KTX-20261102-0501")
                .userId(1L)
                .storeId(1L)
                .finalAmount(BigDecimal.valueOf(85000.00))
                .status(OrderStatus.DELIVERED)
                .paymentMethod(PaymentMethod.VIETQR)
                .paymentStatus(PaymentStatus.PAID)
                .deliveredAt(Instant.now().minusSeconds(3600)) // 1 hour ago
                .build();
    }

    @Test
    @DisplayName("createDispute: Gửi khiếu nại thành công kèm ảnh bằng chứng và số tiền hoàn hợp lệ")
    void createDispute_Success() {
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(disputeRepository.existsByOrderId(501L)).thenReturn(false);
        when(disputeRepository.save(any(DisputeEntity.class))).thenAnswer(inv -> {
            DisputeEntity entity = inv.getArgument(0);
            entity.setId(901L);
            return entity;
        });

        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(501L)
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Hộp cơm bị đổ canh, nắp hộp vỡ nát")
                .evidenceImages(List.of("https://firebasestorage.googleapis.com/.../evidence1.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(45000.00))
                .build();

        DisputeResponse response = disputeService.createDispute(request);

        assertNotNull(response);
        assertEquals(901L, response.getId());
        assertEquals(501L, response.getOrderId());
        assertEquals("KTX-20261102-0501", response.getOrderCode());
        assertEquals(DisputeReason.DAMAGED_FOOD, response.getReason());
        assertEquals(DisputeStatus.PENDING, response.getStatus());
        assertEquals(1, response.getEvidenceImages().size());
        assertEquals(BigDecimal.valueOf(45000.00), response.getRefundRequestedAmount());
        verify(disputeRepository).save(any(DisputeEntity.class));
    }

    @Test
    @DisplayName("createDispute: Thiếu ảnh bằng chứng ném lỗi DISPUTE_EVIDENCE_REQUIRED")
    void createDispute_NoEvidence_ThrowsException() {
        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(501L)
                .reason(DisputeReason.MISSING_ITEM)
                .description("Thiếu nước mắm ngọt")
                .evidenceImages(Collections.emptyList())
                .refundRequestedAmount(BigDecimal.valueOf(10000.00))
                .build();

        AppException ex = assertThrows(AppException.class, () -> disputeService.createDispute(request));
        assertEquals(ErrorCode.DISPUTE_EVIDENCE_REQUIRED, ex.getErrorCode());
        verify(disputeRepository, never()).save(any());
    }

    @Test
    @DisplayName("createDispute: Đơn chưa hoàn tất DELIVERED ném lỗi ORDER_NOT_DELIVERED")
    void createDispute_OrderNotDelivered_ThrowsException() {
        deliveredOrder.setStatus(OrderStatus.DELIVERING);
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);

        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(501L)
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Hộp cơm vỡ")
                .evidenceImages(List.of("https://img.test/pic.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(30000.00))
                .build();

        AppException ex = assertThrows(AppException.class, () -> disputeService.createDispute(request));
        assertEquals(ErrorCode.ORDER_NOT_DELIVERED, ex.getErrorCode());
    }

    @Test
    @DisplayName("createDispute: Số tiền yêu cầu hoàn vượt quá giá trị đơn hàng ném lỗi REFUND_EXCEEDS_ORDER_TOTAL")
    void createDispute_RefundExceedsTotal_ThrowsException() {
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(disputeRepository.existsByOrderId(501L)).thenReturn(false);

        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(501L)
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Yêu cầu hoàn tiền lố")
                .evidenceImages(List.of("https://img.test/pic.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(999000.00)) // > 85000.00
                .build();

        AppException ex = assertThrows(AppException.class, () -> disputeService.createDispute(request));
        assertEquals(ErrorCode.REFUND_EXCEEDS_ORDER_TOTAL, ex.getErrorCode());
    }

    @Test
    @DisplayName("createDispute: Đơn hàng đã có khiếu nại trước đó ném lỗi DISPUTE_ALREADY_EXISTS")
    void createDispute_DuplicateDispute_ThrowsException() {
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(currentUserProvider.getCurrentUserId()).thenReturn(1L);
        when(disputeRepository.existsByOrderId(501L)).thenReturn(true);

        CreateDisputeRequest request = CreateDisputeRequest.builder()
                .orderId(501L)
                .reason(DisputeReason.DAMAGED_FOOD)
                .description("Khiếu nại lần 2")
                .evidenceImages(List.of("https://img.test/pic.jpg"))
                .refundRequestedAmount(BigDecimal.valueOf(50000.00))
                .build();

        AppException ex = assertThrows(AppException.class, () -> disputeService.createDispute(request));
        assertEquals(ErrorCode.DISPUTE_ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    @DisplayName("resolveDispute: Phê duyệt APPROVED khiếu nại và cập nhật tiền hoàn")
    void resolveDispute_Approve_Success() {
        DisputeEntity dispute = DisputeEntity.builder()
                .id(901L)
                .orderId(501L)
                .userId(1L)
                .reason(DisputeReason.DAMAGED_FOOD)
                .refundRequestedAmount(BigDecimal.valueOf(85000.00))
                .status(DisputeStatus.PENDING)
                .build();

        when(disputeRepository.findById(901L)).thenReturn(Optional.of(dispute));
        when(orderRepository.findById(501L)).thenReturn(Optional.of(deliveredOrder));
        when(disputeRepository.save(any(DisputeEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ResolveDisputeRequest request = ResolveDisputeRequest.builder()
                .status(DisputeStatus.APPROVED)
                .refundApprovedAmount(BigDecimal.valueOf(85000.00))
                .resolutionNote("Chấp thuận hoàn tiền 100% qua tài khoản sinh viên")
                .build();

        DisputeResponse response = disputeService.resolveDispute(901L, request);

        assertNotNull(response);
        assertEquals(DisputeStatus.APPROVED, response.getStatus());
        assertEquals(BigDecimal.valueOf(85000.00), response.getRefundApprovedAmount());
        assertEquals(PaymentStatus.REFUNDED, deliveredOrder.getPaymentStatus());
        verify(orderRepository).save(deliveredOrder);
    }
}

