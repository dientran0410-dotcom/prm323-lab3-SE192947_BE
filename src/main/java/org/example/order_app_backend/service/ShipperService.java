package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.ShipperBatchDeliveryResponse;

public interface ShipperService {

    ShipperBatchDeliveryResponse getBatchesBySlot(String slotTime);

    OrderResponse pickupOrder(Long orderId);
}