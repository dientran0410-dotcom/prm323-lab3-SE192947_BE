package org.example.order_app_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.entity.*;
import org.example.order_app_backend.repository.*;
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
    private final OrderItemRepository orderItemRepository;
    private final DisputeRepository disputeRepository;

    @Override
    public void run(ApplicationArguments args) {
        initUsers();
        initStoresAndMenus();
        initSampleOrdersAndDisputes();
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
                    .phone("0918765432")
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

        // 1b. Sinh viên KTX thứ 2 để thử nghiệm luồng gom ghép đơn
        if (userRepository.findByEmail("student2@fpt.edu.vn").isEmpty()) {
            userRepository.save(UserEntity.builder()
                    .firebaseUid("student2_mock_uid")
                    .email("student2@fpt.edu.vn")
                    .phone("0988888222")
                    .displayName("Lê Thùy Linh (Sinh viên 2)")
                    .role(UserRole.STUDENT)
                    .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330")
                    .ktxBuilding("KTX Khu B - Tòa BA2")
                    .ktxRoom("508")
                    .build());
            log.info("Khởi tạo tài khoản Sinh viên 2 (student2@fpt.edu.vn) thành công!");
        }
    }

    private void initStoresAndMenus() {
        UserEntity owner = userRepository.findByEmail("coba.owner@fpt.edu.vn").orElse(null);
        Long ownerId = owner != null ? owner.getId() : 2L;

        // 1. Quán Cơm Tấm Cô Ba - ĐHQG
        if (storeRepository.findAll().stream().noneMatch(s -> s.getName().contains("Cô Ba"))) {
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
                    .name("Cơm Sườn Bì Chả Đặc Biệt")
                    .description("Combo đầy đủ sườn mật ong, bì, chả trứng, tóp mỡ, dưa leo chua ngọt")
                    .imageUrl("https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                    .basePrice(BigDecimal.valueOf(45000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

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
                    .name("Cơm Gà Xối Mỡ Giòn Cay")
                    .description("Đùi gà góc tư da giòn rụm, cơm chiên áp chảo thơm lừng")
                    .imageUrl("https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec")
                    .basePrice(BigDecimal.valueOf(40000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());
        }

        // 2. Quán Bún Bò Huế O Xuân - KTX Khu A
        if (storeRepository.findAll().stream().noneMatch(s -> s.getName().contains("O Xuân"))) {
            StoreEntity store2 = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Bún Bò Huế O Xuân - Khu A")
                    .description("Nước dùng đậm đà sả ớt, nạm bò mềm và chả cua xứ Huế")
                    .address("Đối diện Sảnh Tòa A3 KTX Khu A")
                    .campusZone("KTX_KHU_A")
                    .phoneNumber("0933222333")
                    .avatarUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.80))
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

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store2.getId())
                    .name("Bún Bò Đặc Biệt Thập Cẩm")
                    .description("Thập cẩm bắp hoa bò, gân, giò heo và chả Huế thơm ngon")
                    .imageUrl("https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43")
                    .basePrice(BigDecimal.valueOf(50000.00))
                    .category("BUN_PHO")
                    .isAvailable(true)
                    .customizationOptions(store2Options)
                    .build());
        }

        // 3. Quán Trà Sữa & Cà Phê Muối Sinh Viên
        if (storeRepository.findAll().stream().noneMatch(s -> s.getName().contains("Trà Sữa"))) {
            StoreEntity store3 = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Trà Sữa & Cà Phê Muối Sinh Viên")
                    .description("Trà sữa pha thủ công, cà phê muối béo thơm chuẩn vị sinh viên")
                    .address("Sảnh Tòa BA5 - KTX Khu B")
                    .campusZone("KTX_KHU_B")
                    .phoneNumber("0977112233")
                    .avatarUrl("https://images.unsplash.com/photo-1517256064527-09c73fc73e38")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.92))
                    .ratingCount(230)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store3.getId())
                    .name("Trà Sữa Oolong Nướng Trân Châu")
                    .description("Trà rang thơm lừng quyện cùng trân châu đen dai giòn ngọt bùi")
                    .imageUrl("https://images.unsplash.com/photo-1558857563-b37cf5288219")
                    .basePrice(BigDecimal.valueOf(25000.00))
                    .category("DO_UONG")
                    .isAvailable(true)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store3.getId())
                    .name("Cà Phê Muối Béo Ngậy ĐHQG")
                    .description("Cà phê Robusta đậm đặc lớp kem muối sánh mịn đặc trưng")
                    .imageUrl("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd")
                    .basePrice(BigDecimal.valueOf(20000.00))
                    .category("DO_UONG")
                    .isAvailable(true)
                    .build());
        }

        // 4. Quán Bánh Mì Chảo & Xôi Mặn Cô Út
        if (storeRepository.findAll().stream().noneMatch(s -> s.getName().contains("Bánh Mì Chảo"))) {
            StoreEntity store4 = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Bánh Mì Chảo & Xôi Mặn Cô Út")
                    .description("Bánh mì chảo nóng hổi, xôi pate lạp xưởng phục vụ ăn sáng và tối")
                    .address("Cổng KTX Khu A ĐHQG")
                    .campusZone("KTX_KHU_A")
                    .phoneNumber("0912445566")
                    .avatarUrl("https://images.unsplash.com/photo-1509722747041-616f39b57569")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.85))
                    .ratingCount(95)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store4.getId())
                    .name("Bánh Mì Chảo Xíu Mại Trứng Lòng Đào")
                    .description("Chảo xốt cà chua đậm đà kèm viên xíu mại thịt heo, trứng và pate")
                    .imageUrl("https://images.unsplash.com/photo-1509722747041-616f39b57569")
                    .basePrice(BigDecimal.valueOf(35000.00))
                    .category("AN_VAT")
                    .isAvailable(true)
                    .build());
        }
    }

    private void initSampleOrdersAndDisputes() {
        UserEntity student = userRepository.findByEmail("student@fpt.edu.vn").orElse(null);
        UserEntity shipper = userRepository.findByEmail("shipper.hung@fpt.edu.vn").orElse(null);
        List<StoreEntity> stores = storeRepository.findAll();

        if (student == null || stores.isEmpty()) {
            return;
        }

        StoreEntity storeCoba = stores.stream().filter(s -> s.getName().contains("Cô Ba")).findFirst().orElse(stores.get(0));
        StoreEntity storeBunBo = stores.stream().filter(s -> s.getName().contains("O Xuân")).findFirst().orElse(storeCoba);
        StoreEntity storeTraSua = stores.stream().filter(s -> s.getName().contains("Trà Sữa")).findFirst().orElse(storeCoba);
        StoreEntity storeBanhMi = stores.stream().filter(s -> s.getName().contains("Bánh Mì")).findFirst().orElse(storeCoba);

        // 1. Đơn 1: KTX-ORD-1001 (Đang giao - Cơm Tấm)
        OrderEntity order1 = orderRepository.findByOrderCode("KTX-ORD-1001").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1001")
                    .userId(student.getId())
                    .storeId(storeCoba.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .slotTime("CA_TRUA_11H30")
                    .pickupLocation("KTX Khu B - Sảnh Tòa BA1")
                    .subtotalAmount(BigDecimal.valueOf(85000.00))
                    .deliveryFee(BigDecimal.ZERO)
                    .groupDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(BigDecimal.valueOf(85000.00))
                    .status(OrderStatus.DELIVERING)
                    .paymentMethod(PaymentMethod.VIETQR)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1001")
                    .pickupPin("4829")
                    .note("Để ở bàn tiếp tân sảnh BA1 giúp mình nhé")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(1L)
                    .itemName("Cơm Sườn Bì Chả Đặc Biệt")
                    .unitPrice(BigDecimal.valueOf(45000.00))
                    .quantity(1)
                    .selectedOptions("[{\"group\":\"Tùy chọn vị\",\"choice\":\"Trứng ốp la lòng đào\",\"extraPrice\":0}]")
                    .itemTotalAmount(BigDecimal.valueOf(45000.00))
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(2L)
                    .itemName("Cơm Gà Xối Mỡ Giòn Cay")
                    .unitPrice(BigDecimal.valueOf(40000.00))
                    .quantity(1)
                    .selectedOptions("[{\"group\":\"Chọn phần cơm\",\"choice\":\"Vừa (Tiêu chuẩn)\",\"extraPrice\":0}]")
                    .itemTotalAmount(BigDecimal.valueOf(40000.00))
                    .build());

            return ord;
        });

        // 2. Đơn 2: KTX-ORD-1002 (Đã nhận - Bún Bò Huế)
        OrderEntity order2 = orderRepository.findByOrderCode("KTX-ORD-1002").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1002")
                    .userId(student.getId())
                    .storeId(storeBunBo.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .slotTime("CA_TRUA_11H30")
                    .pickupLocation("KTX Khu B - Sảnh Tòa BA2")
                    .subtotalAmount(BigDecimal.valueOf(50000.00))
                    .deliveryFee(BigDecimal.ZERO)
                    .groupDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(BigDecimal.valueOf(50000.00))
                    .status(OrderStatus.DELIVERED)
                    .paymentMethod(PaymentMethod.VIETQR)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1002")
                    .pickupPin("1934")
                    .deliveredAt(Instant.now().minusSeconds(1800))
                    .note("Bún bò nhiều rau trụng")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(3L)
                    .itemName("Bún Bò Giò Nạm Chả Cua")
                    .unitPrice(BigDecimal.valueOf(40000.00))
                    .quantity(1)
                    .selectedOptions("[{\"group\":\"Kích cỡ tô\",\"choice\":\"Tô lớn (+Bắp bò)\",\"extraPrice\":10000}]")
                    .itemTotalAmount(BigDecimal.valueOf(50000.00))
                    .build());

            return ord;
        });

        // 3. Đơn 3: KTX-ORD-1003 (Đang chuẩn bị - Trà Sữa)
        OrderEntity order3 = orderRepository.findByOrderCode("KTX-ORD-1003").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1003")
                    .userId(student.getId())
                    .storeId(storeTraSua.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .slotTime("CA_TRUA_11H30")
                    .pickupLocation("KTX Khu B - Sảnh Tòa BA5")
                    .subtotalAmount(BigDecimal.valueOf(45000.00))
                    .deliveryFee(BigDecimal.ZERO)
                    .groupDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(BigDecimal.valueOf(45000.00))
                    .status(OrderStatus.PREPARING)
                    .paymentMethod(PaymentMethod.VIETQR)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1003")
                    .pickupPin("7721")
                    .note("70% đường 50% đá")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(4L)
                    .itemName("Trà Sữa Oolong Nướng Trân Châu")
                    .unitPrice(BigDecimal.valueOf(25000.00))
                    .quantity(1)
                    .selectedOptions("[]")
                    .itemTotalAmount(BigDecimal.valueOf(25000.00))
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(5L)
                    .itemName("Cà Phê Muối Béo Ngậy ĐHQG")
                    .unitPrice(BigDecimal.valueOf(20000.00))
                    .quantity(1)
                    .selectedOptions("[]")
                    .itemTotalAmount(BigDecimal.valueOf(20000.00))
                    .build());

            return ord;
        });

        // 4. Đơn 4: KTX-ORD-1004 (Chờ xác nhận - Bánh Mì Chảo)
        orderRepository.findByOrderCode("KTX-ORD-1004").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1004")
                    .userId(student.getId())
                    .storeId(storeBanhMi.getId())
                    .shipperId(null)
                    .slotTime("CA_CHIEU_17H30")
                    .pickupLocation("KTX Khu A - Sảnh Tòa A1")
                    .subtotalAmount(BigDecimal.valueOf(35000.00))
                    .deliveryFee(BigDecimal.ZERO)
                    .groupDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(BigDecimal.valueOf(35000.00))
                    .status(OrderStatus.PENDING)
                    .paymentMethod(PaymentMethod.VIETQR)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1004")
                    .pickupPin("3182")
                    .note("Giao nóng giòn")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(6L)
                    .itemName("Bánh Mì Chảo Xíu Mại Trứng Lòng Đào")
                    .unitPrice(BigDecimal.valueOf(35000.00))
                    .quantity(1)
                    .selectedOptions("[]")
                    .itemTotalAmount(BigDecimal.valueOf(35000.00))
                    .build());

            return ord;
        });

        // Khởi tạo Dispute cho Order 1002 (Đã duyệt hoàn tiền) và Order 1001 (Chờ xử lý)
        if (disputeRepository.findAll().isEmpty()) {
            if (order2 != null) {
                disputeRepository.save(DisputeEntity.builder()
                        .orderId(order2.getId())
                        .userId(student.getId())
                        .reason(DisputeReason.DAMAGED_FOOD)
                        .description("Thức ăn bị chua và thiếu canh rong biển đi kèm.")
                        .evidenceImages("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                        .refundRequestedAmount(BigDecimal.valueOf(45000.00))
                        .refundApprovedAmount(BigDecimal.valueOf(45000.00))
                        .status(DisputeStatus.APPROVED)
                        .resolutionNote("BQL KTX đã xác minh hình ảnh và bồi hoàn 100% qua ví sinh viên.")
                        .build());
            }

            if (order1 != null) {
                disputeRepository.save(DisputeEntity.builder()
                        .orderId(order1.getId())
                        .userId(student.getId())
                        .reason(DisputeReason.WRONG_ITEM)
                        .description("Giao nhầm món so với phiếu đơn hàng đã đặt trên app KTX Food.")
                        .evidenceImages("https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                        .refundRequestedAmount(BigDecimal.valueOf(40000.00))
                        .refundApprovedAmount(null)
                        .status(DisputeStatus.PENDING)
                        .resolutionNote(null)
                        .build());
            }
            log.info("Khởi tạo thành công 2 khiếu nại mẫu thực tế cho Admin & Student!");
        }
    }
}
