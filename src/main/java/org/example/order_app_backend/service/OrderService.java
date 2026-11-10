package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateOrderRequest;
import org.example.order_app_backend.dto.response.OrderResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> getMyOrderHistory();

    PageResponse<OrderResponse> getMyOrders(Pageable pageable);

    OrderResponse cancelOrder(Long id);

    OrderResponse updateOrderStatus(Long orderId, org.example.order_app_backend.dto.request.UpdateOrderStatusRequest request);

    org.example.order_app_backend.dto.response.PickupConfirmationResponse confirmPickup(Long orderId, org.example.order_app_backend.dto.request.ConfirmPickupRequest request);

    OrderResponse notifyArrival(Long orderId);
}

