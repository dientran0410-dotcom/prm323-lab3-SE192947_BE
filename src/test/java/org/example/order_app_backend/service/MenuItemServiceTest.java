package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.request.CreateMenuItemRequest;
import org.example.order_app_backend.dto.request.UpdateMenuItemRequest;
import org.example.order_app_backend.dto.response.CustomizationGroupDto;
import org.example.order_app_backend.dto.response.CustomizationOptionDto;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.MenuItemMapper;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.service.impl.MenuItemServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuItemServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private StoreRepository storeRepository;

    @Spy
    private MenuItemMapper menuItemMapper = new MenuItemMapper();

    @InjectMocks
    private MenuItemServiceImpl menuItemService;

    @Test
    @DisplayName("getMenuItemsByStore: Lấy thực đơn của quán ăn thành công")
    void getMenuItemsByStore_ExistingStore_ShouldReturnItems() {
        when(storeRepository.existsById(1L)).thenReturn(true);

        MenuItemEntity item = MenuItemEntity.builder()
                .id(101L)
                .storeId(1L)
                .name("Cơm Sườn Nướng")
                .basePrice(BigDecimal.valueOf(35000))
                .category("COM")
                .isAvailable(true)
                .build();

        when(menuItemRepository.findByStoreId(1L)).thenReturn(List.of(item));

        List<MenuItemResponse> result = menuItemService.getMenuItemsByStore(1L, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Cơm Sườn Nướng", result.get(0).getName());
        assertEquals(BigDecimal.valueOf(35000), result.get(0).getBasePrice());
    }

    @Test
    @DisplayName("getMenuItemsByStore: Quán không tồn tại -> Ném lỗi ResourceNotFoundException")
    void getMenuItemsByStore_StoreNotFound_ShouldThrowException() {
        when(storeRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> menuItemService.getMenuItemsByStore(999L, null, null));
    }

    @Test
    @DisplayName("createMenuItem: Thêm món mới kèm cấu hình Customization JSON hợp lệ")
    void createMenuItem_ValidRequest_ShouldCreateItem() {
        when(storeRepository.existsById(1L)).thenReturn(true);

        CustomizationOptionDto opt1 = CustomizationOptionDto.builder().name("Trứng ốp la").extraPrice(BigDecimal.valueOf(6000)).build();
        CustomizationGroupDto group1 = CustomizationGroupDto.builder().group("Topping").required(false).options(List.of(opt1)).build();

        CreateMenuItemRequest request = CreateMenuItemRequest.builder()
                .name("Cơm Bì Chả")
                .basePrice(BigDecimal.valueOf(30000))
                .category("COM")
                .customizationOptions(List.of(group1))
                .build();

        MenuItemEntity saved = MenuItemEntity.builder()
                .id(201L)
                .storeId(1L)
                .name("Cơm Bì Chả")
                .basePrice(BigDecimal.valueOf(30000))
                .category("COM")
                .isAvailable(true)
                .customizationOptions("[{\"group\":\"Topping\",\"required\":false,\"options\":[{\"name\":\"Trứng ốp la\",\"extraPrice\":6000}]}]")
                .build();

        when(menuItemRepository.save(any(MenuItemEntity.class))).thenReturn(saved);

        MenuItemResponse response = menuItemService.createMenuItem(1L, request);

        assertNotNull(response);
        assertEquals(201L, response.getId());
        assertEquals("Cơm Bì Chả", response.getName());
        assertNotNull(response.getCustomizationOptions());
        assertEquals(1, response.getCustomizationOptions().size());
        assertEquals("Topping", response.getCustomizationOptions().get(0).getGroup());
    }

    @Test
    @DisplayName("updateMenuItem: Cập nhật tình trạng hết món")
    void updateMenuItem_ExistingItem_ShouldUpdateAvailability() {
        MenuItemEntity item = MenuItemEntity.builder()
                .id(101L)
                .storeId(1L)
                .name("Cơm Sườn")
                .basePrice(BigDecimal.valueOf(35000))
                .isAvailable(true)
                .build();

        when(menuItemRepository.findById(101L)).thenReturn(Optional.of(item));
        when(menuItemRepository.save(any(MenuItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateMenuItemRequest updateRequest = UpdateMenuItemRequest.builder()
                .isAvailable(false)
                .build();

        MenuItemResponse response = menuItemService.updateMenuItem(101L, updateRequest);

        assertNotNull(response);
        assertFalse(response.getIsAvailable());
    }
}

