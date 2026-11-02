package org.example.order_app_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.order_app_backend.dto.common.PageResponse;
import org.example.order_app_backend.dto.request.CreateStoreRequest;
import org.example.order_app_backend.dto.request.UpdateStoreRequest;
import org.example.order_app_backend.dto.response.StoreResponse;
import org.example.order_app_backend.entity.StoreEntity;
import org.example.order_app_backend.exception.ResourceNotFoundException;
import org.example.order_app_backend.mapper.StoreMapper;
import org.example.order_app_backend.repository.StoreRepository;
import org.example.order_app_backend.service.StoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreResponse> getStores(String keyword, String campusZone, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("ratingAvg").descending());

        String cleanedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanedZone = (campusZone != null && !campusZone.isBlank()) ? campusZone.trim() : null;

        Page<StoreEntity> storePage = storeRepository.searchStores(cleanedKeyword, cleanedZone, isActive, pageable);

        List<StoreResponse> responseList = storePage.getContent()
                .stream()
                .map(storeMapper::toResponse)
                .toList();

        return PageResponse.of(responseList, storePage.getNumber(), storePage.getSize(), storePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin quán ăn với ID: " + id));
        return storeMapper.toResponse(store);
    }

    @Override
    @Transactional
    public StoreResponse createStore(CreateStoreRequest request) {
        StoreEntity entity = storeMapper.toEntity(request);
        StoreEntity saved = storeRepository.save(entity);
        log.info("Tạo mới thành công quán ăn ID: {}, Tên: {}", saved.getId(), saved.getName());
        return storeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StoreResponse updateStore(Long id, UpdateStoreRequest request) {
        StoreEntity store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin quán ăn với ID: " + id));

        storeMapper.updateEntityFromRequest(store, request);
        StoreEntity saved = storeRepository.save(store);
        log.info("Cập nhật thành công quán ăn ID: {}", saved.getId());
        return storeMapper.toResponse(saved);
    }
}

