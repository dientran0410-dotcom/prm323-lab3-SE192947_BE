package org.example.order_app_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.OrderRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(ApplicationArguments args) {
        initUsers();
        initStoresAndMenus();
        initSampleOrders();
    }

    private void initUsers() {
        if (userRepository.findByEmail("student@fpt.edu.vn").isEmpty()) {
            log.info("Khởi tạo 4 tài khoản người dùng mẫu ứng với 4 vai trò...");

            // 1. Sinh viên KTX
            userRepository.save(UserEntity.builder()
                    .firebaseUid("student_mock_uid")
                    .email("student@fpt.edu.vn")
                    .phone("0981234567")
                    .displayName("Ngô Trường Đạt (Sinh viên)")
                    .role(UserRole.STUDENT)
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb")
                    .ktxBuilding("KTX Khu B - Tòa BA1")
                    .ktxRoom("312")
                    .build());

            // 2. Chủ Quán ăn KTX (Cơm Tấm Cô Ba)
            userRepository.save(UserEntity.builder()
                    .firebaseUid("coba_owner_mock_uid")
                    .email("coba.owner@fpt.edu.vn")
                    .phone("0908888999")
                    .displayName("Cô Ba (Chủ Quán Cơm Tấm)")
                    .role(UserRole.STORE_OWNER)
                    .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2")
                    .ktxBuilding("Cổng B2 ĐHQG")
                    .ktxRoom("Kiot 01")
                    .build());

            // 3. Shipper Gom đơn KTX
            userRepository.save(UserEntity.builder()
                    .firebaseUid("shipper_hung_mock_uid")
                    .email("shipper.hung@fpt.edu.vn")
                    .phone("0912345678")
                    .displayName("Nguyễn Hùng (Shipper Đội Gom KTX)")
                    .role(UserRole.SHIPPER)
                    .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
                    .ktxBuilding("Đội gom Trụ A1-A2")
                    .ktxRoom("Ca Trưa")
                    .build());

            // 4. Ban Quản trị KTX (Admin)
            userRepository.save(UserEntity.builder()
                    .firebaseUid("admin_ktx_mock_uid")
                    .email("admin.ktx@fpt.edu.vn")
                    .phone("0999000111")
                    .displayName("Ban Quản Lý KTX ĐHQG (Admin)")
                    .role(UserRole.ADMIN)
                    .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e")
                    .ktxBuilding("Văn phòng BQL")
                    .ktxRoom("P.101")
                    .build());

            log.info("Khởi tạo thành công 4 tài khoản cho 4 Role!");
        }
    }

    private void initStoresAndMenus() {
        if (storeRepository.findAll().isEmpty()) {
            log.info("Khởi tạo dữ liệu Quán ăn Cơm Tấm Cô Ba và Bún Bò Huế...");

            UserEntity owner = userRepository.findByEmail("coba.owner@fpt.edu.vn").orElse(null);
            Long ownerId = owner != null ? owner.getId() : 2L;

            // 1. Quán Cơm Tấm Cô Ba - ĐHQG (Chuẩn Figma)
            StoreEntity store1 = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Cơm Tấm Cô Ba - ĐHQG")
                    .description("Cơm tấm sườn nướng mật ong than hồng, chuẩn vị sinh viên KTX")
                    .address("Cổng B2 KTX ĐHQG TP.HCM")
                    .campusZone("KTX_KHU_B")
                    .phoneNumber("0908888999")
                    .avatarUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.90))
                    .ratingCount(168)
                    .build());

            String store1Options = """
                [
                    {
                        "group": "Chọn phần cơm",
                        "required": true,
                        "options": [
                            {"name": "Vừa (Tiêu chuẩn)", "extraPrice": 0},
                            {"name": "Lớn (+Thêm cơm)", "extraPrice": 5000}
                        ]
                    },
                    {
                        "group": "Tùy chọn vị / Topping",
                        "required": false,
                        "options": [
                            {"name": "Trứng ốp la lòng đào", "extraPrice": 6000},
                            {"name": "Chả bì trứng hấp mềm", "extraPrice": 8000},
                            {"name": "Thêm tóp mỡ giòn", "extraPrice": 3000},
                            {"name": "Canh chua thịt bằm", "extraPrice": 5000}
                        ]
                    }
                ]
            """;

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store1.getId())
                    .name("Cơm Tấm Sườn Nướng")
                    .description("Sườn nướng than hoa mềm thơm ướp mật ong tự nhiên")
                    .imageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .basePrice(BigDecimal.valueOf(35000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store1.getId())
                    .name("Cơm Sườn Bì Chả Đặc Biệt")
                    .description("Combo đầy đủ sườn, bì, chả trứng, tóp mỡ, dưa leo chua ngọt")
                    .imageUrl("https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                    .basePrice(BigDecimal.valueOf(45000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store1.getId())
                    .name("Cơm Gà Xối Mỡ Giòn Cay")
                    .description("Đùi gà góc tư da giòn rụm, cơm chiên áp chảo thơm lừng")
                    .imageUrl("https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec")
                    .basePrice(BigDecimal.valueOf(40000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

            // 2. Quán Bún Bò Huế O Xuân - KTX Khu A
            StoreEntity store2 = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Bún Bò Huế O Xuân - Khu A")
                    .description("Nước dùng đậm đà sả ớt, nạm bò mềm và chả cua xứ Huế")
                    .address("Đối diện Sảnh Tòa A3 KTX Khu A")
                    .campusZone("KTX_KHU_A")
                    .phoneNumber("0933222333")
                    .avatarUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.75))
                    .ratingCount(112)
                    .build());

            String store2Options = """
                [
                    {
                        "group": "Kích cỡ tô",
                        "required": true,
                        "options": [
                            {"name": "Tô vừa chuẩn", "extraPrice": 0},
                            {"name": "Tô lớn (+Bắp bò)", "extraPrice": 10000}
                        ]
                    },
                    {
                        "group": "Độ cay & Rau sống",
                        "required": false,
                        "options": [
                            {"name": "Cay vừa", "extraPrice": 0},
                            {"name": "Cay nồng sa tế Huế", "extraPrice": 0},
                            {"name": "Rau trụng chín", "extraPrice": 0}
                        ]
                    }
                ]
            """;

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store2.getId())
                    .name("Bún Bò Giò Nạm Chả Cua")
                    .description("Khoanh giò gân giòn, nạm bò thơm và viên chả cua hấp dẫn")
                    .imageUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624")
                    .basePrice(BigDecimal.valueOf(40000.00))
                    .category("BUN_PHO")
                    .isAvailable(true)
                    .customizationOptions(store2Options)
                    .build());
        }
    }

    private void initSampleOrders() {
        if (orderRepository.findAll().isEmpty()) {
            UserEntity student = userRepository.findByEmail("student@fpt.edu.vn").orElse(null);
            StoreEntity store = storeRepository.findAll().stream().findFirst().orElse(null);
            UserEntity shipper = userRepository.findByEmail("shipper.hung@fpt.edu.vn").orElse(null);

            if (student != null && store != null) {
                // Tạo đơn mẫu KTX-1045 chuẩn theo thiết kế Figma
                orderRepository.save(OrderEntity.builder()
                        .orderCode("KTX-1045")
                        .userId(student.getId())
                        .storeId(store.getId())
                        .shipperId(shipper != null ? shipper.getId() : null)
                        .slotTime("CA_TRUA_11H30")
                        .pickupLocation("KTX Khu B – Trụ A1–A2 (Cổng B2)")
                        .subtotalAmount(BigDecimal.valueOf(40000.00))
                        .deliveryFee(BigDecimal.valueOf(5000.00))
                        .groupDiscountAmount(BigDecimal.ZERO)
                        .finalAmount(BigDecimal.valueOf(45000.00))
                        .status(OrderStatus.DELIVERED)
                        .paymentMethod(PaymentMethod.VIETQR)
                        .paymentStatus(PaymentStatus.PAID)
                        .pickupQrCode("QR_KTX_1045_SAMPLE")
                        .pickupPin("4567")
                        .deliveredAt(Instant.now().minusSeconds(3600))
                        .note("Giao đúng ca trưa 11h30")
                        .build());
            }
        }
    }
}