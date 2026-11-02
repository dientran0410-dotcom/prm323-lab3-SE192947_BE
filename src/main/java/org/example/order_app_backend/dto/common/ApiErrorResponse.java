package org.example.order_app_backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    @Builder.Default
    private Instant timestamp = Instant.now();

    private int status;

    private String errorCode;

    private String message;

    private String path;

    private List<ValidationErrorDetail> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
