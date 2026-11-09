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
    private final ReviewRepository reviewRepository;

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
                    .phone("0968097907")
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
                    .ratingAvg(BigDecimal.valueOf(4.90))
                    .ratingCount(28)
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
        }

        // Cập nhật SĐT mới nhất cho Student 1 (Ngô Trường Đạt)
        userRepository.findByEmail("student@fpt.edu.vn").ifPresent(user -> {
            if (!"0968097907".equals(user.getPhone())) {
                user.setPhone("0968097907");
                userRepository.save(user);
            }
        });
    }

    private void initStoresAndMenus() {
        if (storeRepository.findAll().stream().noneMatch(s -> "Cơm Tấm Cô Ba - KTX Khu B".equals(s.getName()))) {
            log.info("Khởi tạo danh sách Quán ăn KTX & Thực đơn món...");

            UserEntity cobaOwner = userRepository.findByEmail("coba.owner@fpt.edu.vn").orElse(null);
            Long ownerId = (cobaOwner != null) ? cobaOwner.getId() : 2L;

            // Quán 1: Cơm Tấm Cô Ba (Có đầy đủ 4 món theo yêu cầu đồ án)
            StoreEntity storeCoBa = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Cơm Tấm Cô Ba - KTX Khu B")
                    .description("Cơm tấm sườn bì chả chuẩn vị Sài Gòn, phục vụ sinh viên KTX nóng hổi giá bình dân.")
                    .address("Kiot 01, Cổng B2 ĐHQG, Phường Linh Trung, TP. Thủ Đức")
                    .campusZone("KTX_KHU_B")
                    .phoneNumber("0908888999")
                    .avatarUrl("https://images.unsplash.com/photo-1544025162-d76694265947")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.85))
                    .ratingCount(42)
                    .build());

            // Các món ăn Quán 1
            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeCoBa.getId())
                    .name("Cơm Sườn Nướng Mật Ong")
                    .description("Sườn cốt lết nướng than hoa mật ong, cơm tấm thơm dẻo kèm mỡ hành tóp mỡ giòn rụm.")
                    .basePrice(BigDecimal.valueOf(35000.00))
                    .imageUrl("https://images.unsplash.com/photo-1544025162-d76694265947")
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Kích cỡ\",\"required\":true,\"choices\":[{\"name\":\"Phần vừa\",\"extraPrice\":0},{\"name\":\"Phần lớn (+Cơm)\",\"extraPrice\":5000}]},{\"name\":\"Topping thêm\",\"required\":false,\"choices\":[{\"name\":\"Trứng ốp la lòng đào\",\"extraPrice\":5000},{\"name\":\"Chả trứng hấp\",\"extraPrice\":6000},{\"name\":\"Canh khổ qua dồn thịt\",\"extraPrice\":10000}]}]")
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeCoBa.getId())
                    .name("Cơm Tấm Sườn Bì Chả Đặc Biệt")
                    .description("Đầy đủ sườn nướng, bì heo trộn thính thơm và chả trứng muối hấp béo ngậy.")
                    .basePrice(BigDecimal.valueOf(45000.00))
                    .imageUrl("https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Kích cỡ\",\"required\":true,\"choices\":[{\"name\":\"Suất đặc biệt\",\"extraPrice\":0}]},{\"name\":\"Độ chín trứng\",\"required\":false,\"choices\":[{\"name\":\"Trứng lòng đào\",\"extraPrice\":5000},{\"name\":\"Trứng chín kỹ\",\"extraPrice\":5000}]}]")
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeCoBa.getId())
                    .name("Cơm Đùi Gà Xối Mỡ Da Giòn")
                    .description("Đùi gà góc tư chiên xối mỡ vàng ươm, da giòn rụm chấm nước sốt tỏi ớt đặc chế.")
                    .basePrice(BigDecimal.valueOf(38000.00))
                    .imageUrl("https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec")
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Chọn canh đi kèm\",\"required\":true,\"choices\":[{\"name\":\"Canh rong biển thịt bằm\",\"extraPrice\":0},{\"name\":\"Canh chua tôm cay\",\"extraPrice\":5000}]}]")
                    .build());

            // Quán 2: Trà Sữa & Cà Phê KTX
            StoreEntity storeTraSua = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Trà Sữa Sinh Viên & Cà Phê Muối KTX")
                    .description("Thức uống giải khát, trà sữa đậm vị trà và cà phê pha máy cho sinh viên học bài khuya.")
                    .address("Sảnh Tòa BA4 KTX Khu B, ĐHQG")
                    .campusZone("KTX_KHU_B")
                    .phoneNumber("0934567890")
                    .avatarUrl("https://images.unsplash.com/photo-1556881286-fc6915169721")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.70))
                    .ratingCount(25)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeTraSua.getId())
                    .name("Trà Sữa Oolong Nướng Trân Châu")
                    .description("Trà oolong nướng thơm lừng kết hợp sữa tươi thanh trùng và trân châu hoàng kim dai dẻo.")
                    .basePrice(BigDecimal.valueOf(25000.00))
                    .imageUrl("https://images.unsplash.com/photo-1558857563-b37cf5a31a19")
                    .category("DOUONG")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Mức đường\",\"required\":true,\"choices\":[{\"name\":\"100% đường\",\"extraPrice\":0},{\"name\":\"70% đường\",\"extraPrice\":0},{\"name\":\"50% đường (Vừa)\",\"extraPrice\":0},{\"name\":\"30% đường\",\"extraPrice\":0}]},{\"name\":\"Topping\",\"required\":false,\"choices\":[{\"name\":\"Thêm trân châu đen\",\"extraPrice\":5000},{\"name\":\"Thạch củ năng\",\"extraPrice\":6000},{\"name\":\"Kem cheese macchiato\",\"extraPrice\":8000}]}]")
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeTraSua.getId())
                    .name("Cà Phê Muối Béo Ngậy ĐHQG")
                    .description("Cà phê Robusta đậm vị đắng hòa quyện lớp kem muối mặn mặn béo ngậy.")
                    .basePrice(BigDecimal.valueOf(20000.00))
                    .imageUrl("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd")
                    .category("DOUONG")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Đá/Nóng\",\"required\":true,\"choices\":[{\"name\":\"Cà phê đá\",\"extraPrice\":0},{\"name\":\"Cà phê nóng\",\"extraPrice\":0}]}]")
                    .build());

            // Quán 3: Bánh Mì Chảo KTX Khu A
            StoreEntity storeBanhMi = storeRepository.save(StoreEntity.builder()
                    .ownerId(ownerId)
                    .name("Bánh Mì Chảo & Xôi Mặn KTX Khu A")
                    .description("Bánh mì giòn nóng hổi, xíu mại và pate nhà làm thơm ngon chuẩn điểm tâm sinh viên.")
                    .address("Đường A2 KTX Khu A, ĐHQG")
                    .campusZone("KTX_KHU_A")
                    .phoneNumber("0971234888")
                    .avatarUrl("https://images.unsplash.com/photo-1509722747041-616f39b57569")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.60))
                    .ratingCount(18)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(storeBanhMi.getId())
                    .name("Bánh Mì Chảo Xíu Mại Trứng Lòng Đào")
                    .description("Chảo sốt cà chua đậm đà kèm xíu mại viên to, 2 trứng ốp la và pate gan béo ngậy.")
                    .basePrice(BigDecimal.valueOf(35000.00))
                    .imageUrl("https://images.unsplash.com/photo-1509722747041-616f39b57569")
                    .category("BANHMI")
                    .isAvailable(true)
                    .customizationOptions("[{\"name\":\"Kèm thêm\",\"required\":false,\"choices\":[{\"name\":\"Bánh mì thêm (1 ổ)\",\"extraPrice\":5000},{\"name\":\"Xúc xích Đức nướng\",\"extraPrice\":8000}]}]")
                    .build());

            log.info("Khởi tạo hoàn tất 3 quán ăn và các món kèm Topping optionsJson!");
        }
    }

    private void initSampleOrdersAndDisputes() {
        UserEntity student = userRepository.findByEmail("student@fpt.edu.vn").orElse(null);
        UserEntity shipper = userRepository.findByEmail("shipper.hung@fpt.edu.vn").orElse(null);
        List<StoreEntity> allStores = storeRepository.findAll();
        StoreEntity storeCoBa = allStores.stream().filter(s -> "Cơm Tấm Cô Ba - KTX Khu B".equals(s.getName())).findFirst().orElse(null);
        StoreEntity storeTraSua = allStores.stream().filter(s -> "Trà Sữa Sinh Viên & Cà Phê Muối KTX".equals(s.getName())).findFirst().orElse(null);
        StoreEntity storeBanhMi = allStores.stream().filter(s -> "Bánh Mì Chảo & Xôi Mặn KTX Khu A".equals(s.getName())).findFirst().orElse(null);

        if (student == null || storeCoBa == null || storeTraSua == null || storeBanhMi == null) {
            return;
        }

        // 1. Đơn 1: KTX-ORD-1001 (Đang giao DELIVERING)
        OrderEntity order1 = orderRepository.findByOrderCode("KTX-ORD-1001").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1001")
                    .userId(student.getId())
                    .storeId(storeCoBa.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .slotTime("CA_TRUA_11H30")
                    .pickupLocation("KTX Khu B - Trụ A1-A2 (Cổng B2)")
                    .subtotalAmount(BigDecimal.valueOf(40000.00))
                    .deliveryFee(BigDecimal.valueOf(5000.00))
                    .groupDiscountAmount(BigDecimal.valueOf(5000.00))
                    .finalAmount(BigDecimal.valueOf(40000.00))
                    .status(OrderStatus.DELIVERING)
                    .paymentMethod(PaymentMethod.VIETQR)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1001")
                    .pickupPin("4829")
                    .note("Cho thêm ớt và nước mắm ngọt")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(1L)
                    .itemName("Cơm Sườn Nướng Mật Ong")
                    .unitPrice(BigDecimal.valueOf(35000.00))
                    .quantity(1)
                    .selectedOptions("[{\"group\":\"Topping thêm\",\"choice\":\"Trứng ốp la lòng đào\",\"extraPrice\":5000}]")
                    .itemTotalAmount(BigDecimal.valueOf(40000.00))
                    .build());

            return ord;
        });

        // 2. Đơn 2: KTX-ORD-1002 (Đã giao thành công DELIVERED)
        OrderEntity order2 = orderRepository.findByOrderCode("KTX-ORD-1002").orElseGet(() -> {
            OrderEntity ord = orderRepository.save(OrderEntity.builder()
                    .orderCode("KTX-ORD-1002")
                    .userId(student.getId())
                    .storeId(storeCoBa.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .slotTime("CA_TRUA_11H30")
                    .pickupLocation("KTX Khu B - Trụ A1-A2 (Cổng B2)")
                    .subtotalAmount(BigDecimal.valueOf(45000.00))
                    .deliveryFee(BigDecimal.ZERO)
                    .groupDiscountAmount(BigDecimal.ZERO)
                    .finalAmount(BigDecimal.valueOf(45000.00))
                    .status(OrderStatus.DELIVERED)
                    .paymentMethod(PaymentMethod.COD)
                    .paymentStatus(PaymentStatus.PAID)
                    .pickupQrCode("QR_KTX_ORD_1002")
                    .pickupPin("1923")
                    .deliveredAt(Instant.now().minusSeconds(3600))
                    .note("Cơm nhiều mỡ hành")
                    .build());

            orderItemRepository.save(OrderItemEntity.builder()
                    .orderId(ord.getId())
                    .menuItemId(2L)
                    .itemName("Cơm Tấm Sườn Bì Chả Đặc Biệt")
                    .unitPrice(BigDecimal.valueOf(45000.00))
                    .quantity(1)
                    .selectedOptions("[]")
                    .itemTotalAmount(BigDecimal.valueOf(45000.00))
                    .build());

            return ord;
        });

        // 3. Đơn 3: KTX-ORD-1003 (Đang nấu PREPARING - Trà Sữa)
        orderRepository.findByOrderCode("KTX-ORD-1003").orElseGet(() -> {
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

        // Khởi tạo Review mẫu thực tế cho Order 1002 (Đã giao thành công)
        if (order2 != null && !reviewRepository.existsByOrderId(order2.getId())) {
            reviewRepository.save(ReviewEntity.builder()
                    .orderId(order2.getId())
                    .userId(student.getId())
                    .storeId(storeCoBa.getId())
                    .shipperId(shipper != null ? shipper.getId() : null)
                    .storeRating(5)
                    .shipperRating(5)
                    .comment("Cơm sườn nướng mật ong rất ngon, sườn mềm đậm vị, canh nóng hổi. Rất ưng ý!")
                    .shipperComment("Shipper Nguyễn Hùng đến cổng B2 rất đúng giờ, thái độ nhiệt tình lễ phép.")
                    .images("https://images.unsplash.com/photo-1544025162-d76694265947,https://images.unsplash.com/photo-1555396273-367ea4eb4db5")
                    .build());
            log.info("Khởi tạo thành công đánh giá mẫu kèm ảnh cho Order 1002!");
        }

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
