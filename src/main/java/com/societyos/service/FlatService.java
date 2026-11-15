package com.societyos.service;

import com.societyos.dto.request.CreateFlatRequest;
import com.societyos.dto.request.UpdateFlatRequest;
import com.societyos.dto.response.FlatResponse;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlatService {
    FlatResponse createFlat(CreateFlatRequest request);
    FlatResponse updateFlat(Long id, UpdateFlatRequest request);
    FlatResponse getFlatById(Long id);
    Page<FlatResponse> getAllFlats(Long blockId, FlatType flatType, FlatStatus status, Pageable pageable);
    List<FlatResponse> getFlatsByBlockId(Long blockId);
}
