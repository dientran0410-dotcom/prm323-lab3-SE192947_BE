package org.example.order_app_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();

    private record OtpEntry(String otp, Instant expiresAt) {}

    /**
     * Tạo mã OTP ngẫu nhiên 6 chữ số, lưu vào bộ nhớ tạm trong 5 phút và in ra Terminal
     */
    public String generateAndSendOtp(String phoneNumber) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(300); // 5 phút

        otpStorage.put(cleanPhone, new OtpEntry(otp, expiresAt));

        // In ra Terminal theo định dạng cực kỳ nổi bật để người dùng dễ nhìn thấy
        System.out.println("\n" + "=".repeat(75));
        System.out.println("📱 [SMS-GATEWAY-MOCK] ĐÃ GỬI MÃ OTP XÁC THỰC TỚI SỐ: " + cleanPhone);
        System.out.println("💬 NỘI DUNG TIN NHẮN: \"Ma OTP KTX Food cua ban la: " + otp + ". Co hieu luc trong 5 phut.\"");
        System.out.println("🔐 MÃ OTP CẦN ĐIỀN VÀO APP: >>> " + otp + " <<<");
        System.out.println("⏳ THỜI GIAN HẾT HẠN: " + expiresAt);
        System.out.println("=".repeat(75) + "\n");

        log.info("Đã tạo mã OTP cho SĐT: {} (OTP: {})", cleanPhone, otp);
        return otp;
    }

    /**
     * Kiểm tra tính hợp lệ của mã OTP
     */
    public boolean verifyOtp(String phoneNumber, String inputOtp) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        if (inputOtp == null || inputOtp.trim().isEmpty()) {
            return false;
        }

        String trimmedOtp = inputOtp.trim();

        // Chấp nhận mã mặc định phục vụ kiểm thử tự động
        if ("123456".equals(trimmedOtp)) {
            log.info("Chấp nhận mã OTP demo 123456 cho số: {}", cleanPhone);
            return true;
        }

        OtpEntry entry = otpStorage.get(cleanPhone);
        if (entry == null) {
            log.warn("Không tìm thấy mã OTP nào cho số điện thoại: {}", cleanPhone);
            return false;
        }

        if (Instant.now().isAfter(entry.expiresAt())) {
            otpStorage.remove(cleanPhone);
            log.warn("Mã OTP cho số {} đã hết hạn", cleanPhone);
            return false;
        }

        boolean matched = entry.otp().equals(trimmedOtp);
        if (matched) {
            otpStorage.remove(cleanPhone); // Dùng 1 lần là xóa
            log.info("Xác thực OTP thành công cho số: {}", cleanPhone);
            return true;
        }

        log.warn("Mã OTP không khớp cho số {}: nhập {}, đúng là {}", cleanPhone, trimmedOtp, entry.otp());
        return false;
    }

    /**
     * Lấy mã OTP hiện tại còn hiệu lực (hỗ trợ kiểm tra qua Swagger nếu cần)
     */
    public String getLatestOtp(String phoneNumber) {
        String cleanPhone = normalizePhoneNumber(phoneNumber);
        OtpEntry entry = otpStorage.get(cleanPhone);
        if (entry != null && Instant.now().isBefore(entry.expiresAt())) {
            return entry.otp();
        }
        return null;
    }

    private String normalizePhoneNumber(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "").trim();
    }
}

