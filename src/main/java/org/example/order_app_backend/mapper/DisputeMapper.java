package org.example.order_app_backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.response.DisputeResponse;
import org.example.order_app_backend.entity.DisputeEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DisputeMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DisputeResponse toResponse(DisputeEntity entity, String orderCode) {
        if (entity == null) {
            return null;
        }

        List<String> images = parseEvidenceImages(entity.getEvidenceImages());

        return DisputeResponse.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .orderCode(orderCode)
                .userId(entity.getUserId())
                .reason(entity.getReason())
                .description(entity.getDescription())
                .evidenceImages(images)
                .refundRequestedAmount(entity.getRefundRequestedAmount())
                .refundApprovedAmount(entity.getRefundApprovedAmount())
                .status(entity.getStatus())
                .resolutionNote(entity.getResolutionNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<String> parseEvidenceImages(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Không thể giải mã evidence images JSON: {}", json, e);
            return new ArrayList<>();
        }
    }

    public String serializeEvidenceImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            log.warn("Không thể serialize evidence images thành JSON", e);
            return "[]";
        }
    }
}

