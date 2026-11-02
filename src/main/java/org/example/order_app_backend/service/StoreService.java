package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateStoreRequest;
import org.example.order_app_backend.dto.request.UpdateStoreRequest;
import org.example.order_app_backend.dto.response.StoreResponse;

public interface StoreService {

    PageResponse<StoreResponse> getStores(String keyword, String campusZone, Boolean isActive, int page, int size);

    StoreResponse getStoreById(Long id);

    StoreResponse createStore(CreateStoreRequest request);

    StoreResponse updateStore(Long id, UpdateStoreRequest request);
}

