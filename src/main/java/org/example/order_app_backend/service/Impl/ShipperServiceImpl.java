package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.WithdrawalRequest;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.ShipperBatchDeliveryResponse;
import org.example.order_app_backend.dto.response.ShipperWalletResponse;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.OrderMapper;
import org.example.order_app_backend.repository.OrderItemRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.ShipperService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipperServiceImpl implements ShipperService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public ShipperBatchDeliveryResponse getBatchesBySlot(String slotTime) {
        String activeSlot = (slotTime != null && !slotTime.isBlank()) ? slotTime : "CA_TRUA_11H30";

        List<OrderStatus> statuses = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.DELIVERING);
        List<OrderEntity> orders = orderRepository.findBySlotTimeAndStatusInOrderByCreatedAtAsc(activeSlot, statuses);

        if (orders.isEmpty()) {
            orders = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERING || o.getStatus() == OrderStatus.READY || o.getStatus() == OrderStatus.DELIVERED)
                    .toList();
        }

        List<ShipperBatchDeliveryResponse.ShipperOrderItemDto> itemDtos = new ArrayList<>();
        BigDecimal totalDeliveryFee = BigDecimal.ZERO;

        for (OrderEntity order : orders) {
            StoreEntity store = storeRepository.findById(order.getStoreId()).orElse(null);
            UserEntity customer = userRepository.findById(order.getUserId()).orElse(null);

            totalDeliveryFee = totalDeliveryFee.add(order.getDeliveryFee() != null ? order.getDeliveryFee() : BigDecimal.valueOf(5000));

            itemDtos.add(ShipperBatchDeliveryResponse.ShipperOrderItemDto.builder()
                    .orderId(order.getId())
                    .orderCode(order.getOrderCode())
                    .storeName(store != null ? store.getName() : "Quán Cơm KTX")
                    .storeAddress(store != null ? store.getAddress() : "Cổng B2 ĐHQG")
                    .pickupLocation(order.getPickupLocation())
                    .customerName(customer != null ? customer.getDisplayName() : "Sinh viên KTX")
                    .customerPhone(customer != null ? customer.getPhone() : "0981234567")
                    .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "VIETQR")
                    .finalAmount(order.getFinalAmount())
                    .status(order.getStatus())
                    .pickupPin(order.getPickupPin())
                    .createdAt(order.getCreatedAt())
                    .build());
        }

        return ShipperBatchDeliveryResponse.builder()
                .slotTime(activeSlot)
                .campusZone("KTX_KHU_B")
                .totalOrders(itemDtos.size())
                .totalDeliveryEarnings(totalDeliveryFee)
                .orders(itemDtos)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse pickupOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        Long currentUserId = currentUserProvider.getCurrentUserId();
        order.setShipperId(currentUserId != null ? currentUserId : 3L);
        order.setStatus(OrderStatus.DELIVERING);

        OrderEntity saved = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(saved.getId());
        String storeName = storeRepository.findById(saved.getStoreId()).map(StoreEntity::getName).orElse("Quán KTX");

        log.info("Shipper nhận đơn {} chuyển sang DELIVERING", saved.getOrderCode());
        return orderMapper.toResponse(saved, items, storeName);
    }

    @Override
    @Transactional
    public OrderResponse deliverOrder(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.DELIVERED);
        OrderEntity saved = orderRepository.save(order);
        List<OrderItemEntity> items = orderItemRepository.findByOrderId(saved.getId());
        String storeName = storeRepository.findById(saved.getStoreId()).map(StoreEntity::getName).orElse("Quán KTX");

        log.info("Shipper giao thành công đơn {} tại cổng KTX", saved.getOrderCode());
        return orderMapper.toResponse(saved, items, storeName);
    }

    @Override
    public ShipperWalletResponse getWallet() {
        ShipperBatchDeliveryResponse batch = getBatchesBySlot("CA_TRUA_11H30");
        BigDecimal earnings = batch.getTotalDeliveryEarnings();
        BigDecimal codTotal = BigDecimal.ZERO;

        for (ShipperBatchDeliveryResponse.ShipperOrderItemDto ord : batch.getOrders()) {
            if ("COD".equalsIgnoreCase(ord.getPaymentMethod())) {
                codTotal = codTotal.add(ord.getFinalAmount() != null ? ord.getFinalAmount() : BigDecimal.ZERO);
            }
        }

        List<ShipperWalletResponse.WalletTransactionItem> txs = new ArrayList<>();
        txs.add(ShipperWalletResponse.WalletTransactionItem.builder()
                .transactionId("TX-KTX-101")
                .type("CONG_THU_LAO")
                .title("Thù lao ca gom trưa 11:30 KTX Khu B")
                .amount(earnings)
                .createdAt(LocalDateTime.now().minusHours(2))
                .status("SUCCESS")
                .build());

        return ShipperWalletResponse.builder()
                .walletBalance(BigDecimal.valueOf(350000))
                .pendingEarnings(earnings)
                .completedEarnings(BigDecimal.valueOf(350000))
                .completedTrips(12)
                .codCollectedToday(codTotal)
                .recentTransactions(txs)
                .build();
    }

    @Override
    public void withdraw(WithdrawalRequest request) {
        log.info("Shipper yêu cầu rút {} về tài khoản {} - {}", request.getAmount(), request.getBankName(), request.getBankAccountNumber());
    }
}