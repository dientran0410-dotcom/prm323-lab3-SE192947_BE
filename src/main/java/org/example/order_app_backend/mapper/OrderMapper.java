package org.example.order_app_backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.SelectedItemOptionDto;
import org.example.order_app_backend.dto.response.OrderItemResponse;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.entity.OrderEntity;
import org.example.order_app_backend.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class OrderMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderItemResponse toItemResponse(OrderItemEntity entity) {
        if (entity == null) {
            return null;
        }

        List<SelectedItemOptionDto> options = parseSelectedOptions(entity.getSelectedOptions());

        return OrderItemResponse.builder()
                .id(entity.getId())
                .menuItemId(entity.getMenuItemId())
                .itemName(entity.getItemName())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .selectedOptions(options)
                .itemTotalAmount(entity.getItemTotalAmount())
                .build();
    }

    public List<OrderItemResponse> toItemResponses(List<OrderItemEntity> entities) {
        if (entities == null) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::toItemResponse).toList();
    }

    public OrderResponse toResponse(OrderEntity order, List<OrderItemEntity> items, String storeName) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponse> itemResponses = toItemResponses(items);

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .storeId(order.getStoreId())
                .storeName(storeName)
                .slotTime(order.getSlotTime())
                .pickupLocation(order.getPickupLocation())
                .subtotalAmount(order.getSubtotalAmount())
                .deliveryFee(order.getDeliveryFee())
                .groupDiscountAmount(order.getGroupDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .pickupQrCode(order.getPickupQrCode())
                .pickupPin(order.getPickupPin())
                .note(order.getNote())
                .shipperArrived(order.getShipperArrived() != null && order.getShipperArrived())
                .items(itemResponses)
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<SelectedItemOptionDto> parseSelectedOptions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<SelectedItemOptionDto>>() {});
        } catch (Exception e) {
            log.warn("Không thể giải mã selected options: {}", json, e);
            return new ArrayList<>();
        }
    }

    public String serializeSelectedOptions(List<SelectedItemOptionDto> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            log.warn("Không thể chuyển đổi selected options thành chuỗi JSON", e);
            return "[]";
        }
    }
}

