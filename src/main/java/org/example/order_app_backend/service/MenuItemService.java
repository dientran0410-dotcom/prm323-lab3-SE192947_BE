package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.CreateMenuItemRequest;
import org.example.order_app_backend.dto.request.UpdateMenuItemRequest;
import org.example.order_app_backend.dto.response.MenuItemResponse;

import java.util.List;

public interface MenuItemService {

    List<MenuItemResponse> getMenuItemsByStore(Long storeId, String category, Boolean availableOnly);

    MenuItemResponse getMenuItemById(Long id);

    MenuItemResponse createMenuItem(Long storeId, CreateMenuItemRequest request);

    MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request);

    void deleteMenuItem(Long id);
}

