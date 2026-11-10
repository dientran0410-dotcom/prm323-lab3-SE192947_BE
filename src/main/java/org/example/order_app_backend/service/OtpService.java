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

        // In ra Terminal theo định dạng cực kỳ nổi bật để người dùng trên VPS nhìn thấy ngay lập tức
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔥🔥🔥 [KTX-FOOD-OTP] MÃ XÁC THỰC OTP ĐỔI MẬT KHẨU CHO SĐT: " + cleanPhone + " 🔥🔥🔥");
        System.out.println("👉 MÃ OTP CỦA BẠN LÀ:        >>>  " + otp + "  <<<");
        System.out.println("👉 THỜI GIAN CÓ HIỆU LỰC:   5 PHÚT (Hết hạn lúc: " + expiresAt + ")");
        System.out.println("💡 GỢI Ý TEST NHANH:         App cũng chấp nhận mã mặc định '123456'");
        System.out.println("=".repeat(80) + "\n");

        // Ghi ra file latest_otp.txt để các script trên VPS đọc liền tức thì
        try {
            String otpLog = String.format("[%s] SĐT: %s | MÃ OTP: %s | HẾT HẠN: %s%n",
                    java.time.LocalDateTime.now(), cleanPhone, otp, expiresAt);
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("latest_otp.txt"),
                    otpLog,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            java.nio.file.Files.writeString(
                    java.nio.file.Path.of("otp_history.log"),
                    otpLog,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
            // Thử ghi vào /tmp/latest_otp.txt nếu chạy trên Linux VPS
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("/tmp/latest_otp.txt"),
                        otpLog,
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            log.warn("Không thể ghi file latest_otp.txt: {}", e.getMessage());
        }

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

