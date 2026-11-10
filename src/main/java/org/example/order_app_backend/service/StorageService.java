package org.example.order_app_backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Tải lên ảnh đại diện của người dùng lên Cloud Storage (Cloudinary)
     *
     * @param file   Tập tin hình ảnh (chỉ chấp nhận JPEG, PNG, WEBP)
     * @param userId ID người dùng
     * @return URL an toàn (HTTPS) trỏ tới ảnh đại diện đã lưu trên Cloud
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * Xóa ảnh đại diện khỏi Cloud Storage nếu tồn tại
     *
     * @param avatarUrl Đường dẫn URL của ảnh cần xóa
     */
    void deleteAvatar(String avatarUrl);
}
