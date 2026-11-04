package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.WithdrawalRequest;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.example.order_app_backend.dto.response.ShipperBatchDeliveryResponse;
import org.example.order_app_backend.dto.response.ShipperWalletResponse;

public interface ShipperService {

    ShipperBatchDeliveryResponse getBatchesBySlot(String slotTime);

    OrderResponse pickupOrder(Long orderId);

    OrderResponse deliverOrder(Long orderId);

    ShipperWalletResponse getWallet();

    void withdraw(WithdrawalRequest request);
}