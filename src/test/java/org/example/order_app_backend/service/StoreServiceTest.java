package org.example.order_app_backend.service;

import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateStoreRequest;
import org.example.order_app_backend.dto.request.UpdateStoreRequest;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.StoreMapper;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Spy
    private StoreMapper storeMapper = new StoreMapper();

    @InjectMocks
    private StoreServiceImpl storeService;

    @Test
    @DisplayName("getStores: Tìm kiếm quán ăn theo phân khu và từ khóa thành công")
    void getStores_ShouldReturnPageResponse() {
        StoreEntity entity = StoreEntity.builder()
                .id(1L)
                .name("Cơm Tấm B2")
                .campusZone("KTX_KHU_B")
                .address("Cổng B2")
                .ratingAvg(BigDecimal.valueOf(4.8))
                .ratingCount(50)
                .isActive(true)
                .build();

        Page<StoreEntity> pagedResult = new PageImpl<>(List.of(entity));

        when(storeRepository.searchStores(eq("Cơm"), eq("KTX_KHU_B"), eq(true), any(Pageable.class)))
                .thenReturn(pagedResult);

        PageResponse<StoreResponse> result = storeService.getStores("Cơm", "KTX_KHU_B", true, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Cơm Tấm B2", result.getContent().get(0).getName());
        assertEquals("KTX_KHU_B", result.getContent().get(0).getCampusZone());
    }

    @Test
    @DisplayName("getStoreById: Tìm thấy quán ăn theo ID")
    void getStoreById_ExistingId_ShouldReturnStore() {
        StoreEntity entity = StoreEntity.builder()
                .id(1L)
                .name("Cơm Tấm B2")
                .address("Cổng B2")
                .campusZone("KTX_KHU_B")
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(entity));

        StoreResponse response = storeService.getStoreById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Cơm Tấm B2", response.getName());
    }

    @Test
    @DisplayName("getStoreById: Không tìm thấy quán ăn -> Ném lỗi ResourceNotFoundException")
    void getStoreById_NotFound_ShouldThrowException() {
        when(storeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> storeService.getStoreById(999L));
    }

    @Test
    @DisplayName("createStore: Tạo quán ăn mới hợp lệ")
    void createStore_ValidRequest_ShouldCreateStore() {
        CreateStoreRequest request = CreateStoreRequest.builder()
                .name("Trà Sữa KTX A")
                .address("Tòa A1")
                .campusZone("KTX_KHU_A")
                .build();

        StoreEntity saved = StoreEntity.builder()
                .id(5L)
                .name("Trà Sữa KTX A")
                .address("Tòa A1")
                .campusZone("KTX_KHU_A")
                .isActive(true)
                .build();

        when(storeRepository.save(any(StoreEntity.class))).thenReturn(saved);

        StoreResponse response = storeService.createStore(request);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Trà Sữa KTX A", response.getName());
    }

    @Test
    @DisplayName("updateStore: Cập nhật thành công thông tin quán")
    void updateStore_ExistingStore_ShouldUpdate() {
        StoreEntity entity = StoreEntity.builder()
                .id(1L)
                .name("Cơm Tấm Cũ")
                .address("Cổng B2")
                .campusZone("KTX_KHU_B")
                .isActive(true)
                .build();

        when(storeRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(storeRepository.save(any(StoreEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateStoreRequest updateRequest = UpdateStoreRequest.builder()
                .name("Cơm Tấm Đêm B2 (Cập nhật)")
                .isActive(false)
                .build();

        StoreResponse response = storeService.updateStore(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Cơm Tấm Đêm B2 (Cập nhật)", response.getName());
        assertFalse(response.getIsActive());
    }
}

