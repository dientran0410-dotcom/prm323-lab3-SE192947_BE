package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.request.CreateMenuItemRequest;
import org.example.order_app_backend.dto.request.UpdateMenuItemRequest;
import org.example.order_app_backend.dto.response.MenuItemResponse;
import org.example.order_app_backend.entity.MenuItemEntity;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.MenuItemMapper;
import org.example.order_app_backend.repository.MenuItemRepository;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.service.MenuItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final StoreRepository storeRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByStore(Long storeId, String category, Boolean availableOnly) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin quán ăn với ID: " + storeId);
        }

        List<MenuItemEntity> items;
        String cleanCategory = (category != null && !category.isBlank()) ? category.trim().toUpperCase() : null;

        if (cleanCategory != null && Boolean.TRUE.equals(availableOnly)) {
            items = menuItemRepository.findByStoreIdAndCategoryAndIsAvailable(storeId, cleanCategory, true);
        } else if (cleanCategory != null) {
            items = menuItemRepository.findByStoreIdAndCategory(storeId, cleanCategory);
        } else if (Boolean.TRUE.equals(availableOnly)) {
            items = menuItemRepository.findByStoreIdAndIsAvailable(storeId, true);
        } else {
            items = menuItemRepository.findByStoreId(storeId);
        }

        return items.stream()
                .map(menuItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        MenuItemEntity item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + id));
        return menuItemMapper.toResponse(item);
    }

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(Long storeId, CreateMenuItemRequest request) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Không tìm thấy quán ăn để thêm món với ID: " + storeId);
        }

        MenuItemEntity entity = menuItemMapper.toEntity(storeId, request);
        MenuItemEntity saved = menuItemRepository.save(entity);
        log.info("Thêm món mới thành công ID: {}, Món: {} cho quán ID: {}", saved.getId(), saved.getName(), storeId);
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request) {
        MenuItemEntity item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + id));

        menuItemMapper.updateEntityFromRequest(item, request);
        MenuItemEntity saved = menuItemRepository.save(item);
        log.info("Cập nhật thành công món ăn ID: {}", saved.getId());
        return menuItemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy món ăn với ID: " + id);
        }
        menuItemRepository.deleteById(id);
        log.info("Xóa thành công món ăn ID: {}", id);
    }
}

