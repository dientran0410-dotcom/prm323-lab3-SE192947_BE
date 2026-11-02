package org.example.order_app_backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final StoreRepository storeRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (storeRepository.findAll().isEmpty()) {
            log.info("Khởi tạo dữ liệu mẫu cho Quán ăn và Thực đơn KTX...");

            // 1. Quán Cơm Tấm Sườn Bì KTX B2
            StoreEntity store1 = storeRepository.save(StoreEntity.builder()
                    .name("Cơm Tấm Sườn Bì KTX B2")
                    .description("Cơm tấm đêm sinh viên nóng sốt, phục vụ nhanh tận Trụ B2")
                    .address("Cổng B2 KTX ĐHQG")
                    .campusZone("KTX_KHU_B")
                    .phoneNumber("0987111222")
                    .avatarUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.85))
                    .ratingCount(142)
                    .build());

            String store1Options = """
                [
                    {
                        "group": "Kích cỡ",
                        "required": true,
                        "options": [
                            {"name": "Vừa (Tiêu chuẩn)", "extraPrice": 0},
                            {"name": "Lớn (+Thêm cơm)", "extraPrice": 5000}
                        ]
                    },
                    {
                        "group": "Topping thêm",
                        "required": false,
                        "options": [
                            {"name": "Trứng ốp la", "extraPrice": 6000},
                            {"name": "Chả bì trứng", "extraPrice": 8000},
                            {"name": "Canh rong biển thêm", "extraPrice": 5000}
                        ]
                    }
                ]
            """;

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store1.getId())
                    .name("Cơm Sườn Nướng Chả Trứng")
                    .description("Sườn ướp mật ong than hồng kèm chả trứng hấp mềm")
                    .imageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .basePrice(BigDecimal.valueOf(35000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store1.getId())
                    .name("Cơm Sườn Bì Chả Đặc Biệt")
                    .description("Sườn nướng, bì sợi, chả trứng, tóp mỡ giòn rụm")
                    .imageUrl("https://images.unsplash.com/photo-1546069901-ba9599a7e63c")
                    .basePrice(BigDecimal.valueOf(42000.00))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions(store1Options)
                    .build());

            // 2. Quán Bún Bò Huế Hương Giang - KTX Khu A
            StoreEntity store2 = storeRepository.save(StoreEntity.builder()
                    .name("Bún Bò Huế Hương Giang - KTX Khu A")
                    .description("Nước dùng đậm đà, thịt bò mềm và chả cua xứ Huế")
                    .address("Sảnh A3 KTX Khu A")
                    .campusZone("KTX_KHU_A")
                    .phoneNumber("0933222333")
                    .avatarUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.70))
                    .ratingCount(98)
                    .build());

            String store2Options = """
                [
                    {
                        "group": "Kích cỡ",
                        "required": true,
                        "options": [
                            {"name": "Tô vừa", "extraPrice": 0},
                            {"name": "Tô lớn (+Bò)", "extraPrice": 10000}
                        ]
                    },
                    {
                        "group": "Độ cay",
                        "required": false,
                        "options": [
                            {"name": "Không cay", "extraPrice": 0},
                            {"name": "Cay vừa", "extraPrice": 0},
                            {"name": "Cay nồng", "extraPrice": 0}
                        ]
                    }
                ]
            """;

            menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(store2.getId())
                    .name("Bún Bò Nạm Chả Cua")
                    .description("Nạm bò mềm thơm, viên chả cua giòn ngọt, nước lèo cay the")
                    .imageUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624")
                    .basePrice(BigDecimal.valueOf(40000.00))
                    .category("BUN_PHO")
                    .isAvailable(true)
                    .customizationOptions(store2Options)
                    .build());

            log.info("Khởi tạo thành công 2 quán ăn mẫu và 3 món ăn có tùy biến.");
        }
    }
}

