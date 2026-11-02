package org.example.order_app_backend.service.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class VietQrPaymentGateway implements PaymentGateway {

    private static final String DEFAULT_BANK_BIN = "970422"; // MB Bank
    private static final String DEFAULT_BANK_NAME = "MBBank";
    private static final String DEFAULT_ACCOUNT_NO = "0987654321";
    private static final String DEFAULT_ACCOUNT_NAME = "KTX FOOD ORDERING";

    @Override
    public PaymentMethod getSupportedMethod() {
        return PaymentMethod.VIETQR;
    }

    @Override
    public PaymentQrResult generatePaymentQr(Long orderId, String orderCode, BigDecimal amount) {
        log.info("Khởi tạo mã VietQR Napas 247 cho đơn hàng ID: {}, số tiền: {}", orderId, amount);

        // BR-PAY-01: Chuỗi nội dung chuyển khoản bắt buộc theo định dạng ORD_<ORDER_ID>
        String vietqrContent = "ORD_" + orderId;
        long amountLong = amount != null ? amount.longValue() : 0L;

        String encodedContent = URLEncoder.encode(vietqrContent, StandardCharsets.UTF_8);
        String encodedAccountName = URLEncoder.encode(DEFAULT_ACCOUNT_NAME, StandardCharsets.UTF_8);

        // Sinh liên kết ảnh mã QR VietQR chuẩn Napas 247 QuickLink
        String qrUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                DEFAULT_BANK_BIN, DEFAULT_ACCOUNT_NO, amountLong, encodedContent, encodedAccountName);

        return PaymentQrResult.builder()
                .qrUrl(qrUrl)
                .vietqrContent(vietqrContent)
                .bankBin(DEFAULT_BANK_BIN)
                .bankName(DEFAULT_BANK_NAME)
                .accountNumber(DEFAULT_ACCOUNT_NO)
                .accountName(DEFAULT_ACCOUNT_NAME)
                .amount(amount)
                .build();
    }
}

