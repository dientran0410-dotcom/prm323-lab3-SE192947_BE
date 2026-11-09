package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.UpdateMenuItemAvailabilityRequest;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.dto.response.OwnerOrderSummaryResponse;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.entity.OrderItemEntity;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.entity.UserEntity;
import org.example.order_app_backend.exception.AppException;
import org.example.order_app_backend.exception.ErrorCode;
import org.example.order_app_backend.mapper.MenuItemMapper;
import org.example.order_app_backend.mapper.StoreMapper;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.OrderItemRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.example.order_app_backend.security.CurrentUserProvider;
import org.example.order_app_backend.service.StoreOwnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.order_app_backend.dto.response.StoreRevenueResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOwnerServiceImpl implements StoreOwnerService {

    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final StoreMapper storeMapper;
    private final MenuItemMapper menuItemMapper;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public StoreResponse getMyStore() {
        StoreEntity store = resolveCurrentStore();
        return storeMapper.toResponse(store);
    }

    @Override
    public List<OwnerOrderSummaryResponse> getMyStoreOrders(String slotTime) {
        StoreEntity store = resolveCurrentStore();
        List<OrderEntity> orders;
        if (slotTime != null && !slotTime.isBlank()) {
            orders = orderRepository.findByStoreIdAndSlotTimeOrderByCreatedAtDesc(store.getId(), slotTime);
        } else {
            orders = orderRepository.findByStoreIdAndSlotTimeOrderByCreatedAtDesc(store.getId(), "CA_TRUA_11H30");
            if (orders.isEmpty()) {
                orders = orderRepository.findAll().stream()
                        .filter(o -> o.getStoreId().equals(store.getId()))
                        .toList();
            }
        }

        List<OwnerOrderSummaryResponse> responseList = new ArrayList<>();
        for (OrderEntity order : orders) {
            UserEntity customer = userRepository.findById(order.getUserId()).orElse(null);
            List<OrderItemEntity> items = orderItemRepository.findByOrderId(order.getId());

            List<OwnerOrderSummaryResponse.OwnerOrderItemDto> itemDtos = items.stream()
                    .map(item -> OwnerOrderSummaryResponse.OwnerOrderItemDto.builder()
                            .itemName(item.getItemName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .itemTotalAmount(item.getItemTotalAmount())
                            .selectedOptions(item.getSelectedOptions())
                            .build())
                    .toList();

            responseList.add(OwnerOrderSummaryResponse.builder()
                    .id(order.getId())
                    .orderCode(order.getOrderCode())
                    .customerName(customer != null ? customer.getDisplayName() : "Sinh viên KTX")
                    .customerPhone(customer != null ? customer.getPhone() : "0968097907")
                    .slotTime(order.getSlotTime())
                    .pickupLocation(order.getPickupLocation())
                    .finalAmount(order.getFinalAmount())
                    .status(order.getStatus())
                    .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "VIETQR")
                    .note(order.getNote())
                    .createdAt(order.getCreatedAt())
                    .items(itemDtos)
                    .build());
        }

        return responseList;
    }

    @Override
    public StoreRevenueResponse getStoreRevenue(String slotTime) {
        StoreEntity store = resolveCurrentStore();
        List<OwnerOrderSummaryResponse> orders = getMyStoreOrders(slotTime);
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal codAmount = BigDecimal.ZERO;
        BigDecimal onlineAmount = BigDecimal.ZERO;
        int completedCount = 0;

        for (OwnerOrderSummaryResponse ord : orders) {
            BigDecimal amt = ord.getFinalAmount() != null ? ord.getFinalAmount() : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(amt);
            if ("COD".equalsIgnoreCase(ord.getPaymentMethod())) {
                codAmount = codAmount.add(amt);
            } else {
                onlineAmount = onlineAmount.add(amt);
            }
            if (ord.getStatus() != null && ("DELIVERED".equals(ord.getStatus().name()) || "PICKED_UP".equals(ord.getStatus().name()))) {
                completedCount++;
            }
        }

        return StoreRevenueResponse.builder()
                .shiftName(slotTime != null ? slotTime : "CA_TRUA_11H30")
                .totalRevenue(totalRevenue)
                .codAmount(codAmount)
                .onlineAmount(onlineAmount)
                .totalOrders(orders.size())
                .completedOrders(completedCount)
                .orderDetails(orders)
                .build();
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItemAvailability(Long menuItemId, UpdateMenuItemAvailabilityRequest request) {
        StoreEntity store = resolveCurrentStore();
        MenuItemEntity menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_ITEM_NOT_FOUND));

        if (!menuItem.getStoreId().equals(store.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        menuItem.setIsAvailable(request.getIsAvailable());
        MenuItemEntity saved = menuItemRepository.save(menuItem);
        log.info("Quán {} cập nhật món {} trạng thái khả dụng sang: {}", store.getName(), saved.getName(), saved.getIsAvailable());
        return menuItemMapper.toResponse(saved);
    }

    private StoreEntity resolveCurrentStore() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId != null) {
            StoreEntity store = storeRepository.findByOwnerId(currentUserId).stream().findFirst().orElse(null);
            if (store != null) {
                return store;
            }
        }
        return storeRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.STORE_NOT_FOUND));
    }
}