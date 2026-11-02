package org.example.order_app_backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.CreateMenuItemRequest;
import org.example.order_app_backend.dto.request.UpdateMenuItemRequest;
import org.example.order_app_backend.dto.response.CustomizationGroupDto;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MenuItemMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MenuItemResponse toResponse(MenuItemEntity entity) {
        if (entity == null) {
            return null;
        }

        List<CustomizationGroupDto> options = parseCustomizationOptions(entity.getCustomizationOptions());

        return MenuItemResponse.builder()
                .id(entity.getId())
                .storeId(entity.getStoreId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .basePrice(entity.getBasePrice())
                .category(entity.getCategory())
                .isAvailable(entity.getIsAvailable())
                .customizationOptions(options)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MenuItemEntity toEntity(Long storeId, CreateMenuItemRequest request) {
        if (request == null) {
            return null;
        }

        String jsonOptions = serializeCustomizationOptions(request.getCustomizationOptions());

        return MenuItemEntity.builder()
                .storeId(storeId)
                .name(request.getName().trim())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .basePrice(request.getBasePrice())
                .category(request.getCategory().trim().toUpperCase())
                .isAvailable(true)
                .customizationOptions(jsonOptions)
                .build();
    }

    public void updateEntityFromRequest(MenuItemEntity entity, UpdateMenuItemRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) {
            entity.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }
        if (request.getBasePrice() != null) {
            entity.setBasePrice(request.getBasePrice());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            entity.setCategory(request.getCategory().trim().toUpperCase());
        }
        if (request.getIsAvailable() != null) {
            entity.setIsAvailable(request.getIsAvailable());
        }
        if (request.getCustomizationOptions() != null) {
            entity.setCustomizationOptions(serializeCustomizationOptions(request.getCustomizationOptions()));
        }
    }

    private List<CustomizationGroupDto> parseCustomizationOptions(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<CustomizationGroupDto>>() {});
        } catch (Exception e) {
            log.warn("Không thể giải mã JSON customization options: {}", json, e);
            return new ArrayList<>();
        }
    }

    private String serializeCustomizationOptions(List<CustomizationGroupDto> options) {
        if (options == null || options.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            log.warn("Không thể chuyển đổi danh sách options thành chuỗi JSON", e);
            return "[]";
        }
    }
}

