package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.UpdateMenuItemAvailabilityRequest;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.dto.response.OwnerOrderSummaryResponse;
import org.example.order_app_backend.dto.response.StoreResponse;

import org.example.order_app_backend.dto.response.StoreRevenueResponse;

import java.util.List;

public interface StoreOwnerService {

    StoreResponse getMyStore();

    List<OwnerOrderSummaryResponse> getMyStoreOrders(String slotTime);

    StoreRevenueResponse getStoreRevenue(String slotTime);

    MenuItemResponse updateMenuItemAvailability(Long menuItemId, UpdateMenuItemAvailabilityRequest request);
}