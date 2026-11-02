package org.example.order_app_backend.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
