package org.example.order_app_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private StoreEntity testStore;
    private MenuItemEntity testMenuItem;

    @BeforeEach
    void setUp() {
        if (storeRepository.findAll().isEmpty()) {
            testStore = storeRepository.save(StoreEntity.builder()
                    .name("Cơm Tấm B2 Test")
                    .description("Cơm tấm sinh viên nóng sốt")
                    .address("Cổng B2 KTX")
                    .campusZone("KTX_KHU_B")
                    .isActive(true)
                    .ratingAvg(BigDecimal.valueOf(4.90))
                    .ratingCount(80)
                    .build());

            testMenuItem = menuItemRepository.save(MenuItemEntity.builder()
                    .storeId(testStore.getId())
                    .name("Cơm Sườn Nướng Chả Trứng")
                    .basePrice(BigDecimal.valueOf(35000))
                    .category("COM")
                    .isAvailable(true)
                    .customizationOptions("[{\"group\":\"Size\",\"required\":true,\"options\":[{\"name\":\"Lớn\",\"extraPrice\":5000}]}]")
                    .build());
        } else {
            testStore = storeRepository.findAll().get(0);
            testMenuItem = menuItemRepository.findByStoreId(testStore.getId()).get(0);
        }
    }

    @Test
    @DisplayName("GET /api/v1/stores - Happy Path: Lấy danh sách quán ăn thành công")
    void getStores_ShouldReturnPageOfStores() throws Exception {
        mockMvc.perform(get("/api/v1/stores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", not(empty())))
                .andExpect(jsonPath("$.data.pageNumber").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/stores?campusZone=KTX_KHU_B - Lọc theo phân khu KTX Khu B")
    void getStores_FilterByZone_ShouldReturnFilteredStores() throws Exception {
        mockMvc.perform(get("/api/v1/stores")
                        .param("campusZone", "KTX_KHU_B"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].campusZone").value("KTX_KHU_B"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{id} - Happy Path: Xem chi tiết quán ăn")
    void getStoreById_ExistingId_ShouldReturnStore() throws Exception {
        mockMvc.perform(get("/api/v1/stores/{id}", testStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testStore.getId()))
                .andExpect(jsonPath("$.data.name").value(testStore.getName()));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{id} - Error Path: Quán không tồn tại trả về HTTP 404")
    void getStoreById_NotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/stores/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("GET /api/v1/stores/{storeId}/menu-items - Happy Path: Lấy thực đơn của quán")
    void getMenuItemsByStore_ShouldReturnItems() throws Exception {
        mockMvc.perform(get("/api/v1/stores/{storeId}/menu-items", testStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].storeId").value(testStore.getId()))
                .andExpect(jsonPath("$.data[0].name").value(testMenuItem.getName()));
    }

    @Test
    @DisplayName("GET /api/v1/menu-items/{id} - Happy Path: Lấy chi tiết món ăn kèm customization options")
    void getMenuItemById_ShouldReturnItemWithCustomizations() throws Exception {
        mockMvc.perform(get("/api/v1/menu-items/{id}", testMenuItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testMenuItem.getId()))
                .andExpect(jsonPath("$.data.customizationOptions", not(empty())))
                .andExpect(jsonPath("$.data.customizationOptions[0].group").isNotEmpty());
    }
}
