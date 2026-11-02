package org.example.order_app_backend.exception;

public class BusinessRuleException extends AppException {

    public BusinessRuleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessRuleException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
