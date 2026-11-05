package org.example.order_app_backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DisputeReason {
    DAMAGED_FOOD,
    MISSING_ITEM,
    WRONG_ITEM,
    LATE_DELIVERY,
    OTHER;

    @JsonCreator
    public static DisputeReason fromString(String value) {
        if (value == null || value.isBlank()) return OTHER;
        String upper = value.trim().toUpperCase();
        for (DisputeReason r : values()) {
            if (r.name().equals(upper)) return r;
        }
        if (value.contains("dị vật") || value.contains("Hỏng") || value.contains("hỏng") || value.contains("đổ") || value.contains("tràn") || value.contains("Hư hại")) {
            return DAMAGED_FOOD;
        }
        if (value.contains("thiếu") || value.contains("Thiếu")) {
            return MISSING_ITEM;
        }
        if (value.contains("sai") || value.contains("Sai")) {
            return WRONG_ITEM;
        }
        if (value.contains("muộn") || value.contains("trễ") || value.contains("Shipper") || value.contains("trụ")) {
            return LATE_DELIVERY;
        }
        return OTHER;
    }
}

