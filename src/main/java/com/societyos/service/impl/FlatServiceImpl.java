package com.societyos.service.impl;

import com.societyos.dto.request.CreateFlatRequest;
import com.societyos.dto.request.UpdateFlatRequest;
import com.societyos.dto.response.FlatResponse;
import com.societyos.entity.Block;
import com.societyos.entity.Flat;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.BlockRepository;
import com.societyos.repository.FlatRepository;
import com.societyos.service.FlatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlatServiceImpl implements FlatService {

    private static final Logger log = LoggerFactory.getLogger(FlatServiceImpl.class);

    private final FlatRepository flatRepository;
    private final BlockRepository blockRepository;

    public FlatServiceImpl(FlatRepository flatRepository, BlockRepository blockRepository) {
        this.flatRepository = flatRepository;
        this.blockRepository = blockRepository;
    }

    @Override
    @Transactional
    public FlatResponse createFlat(CreateFlatRequest request) {
        Block block = blockRepository.findById(request.blockId())
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", request.blockId()));

        String normalizedNumber = request.flatNumber().trim().toUpperCase();
        if (flatRepository.existsByBlockIdAndFlatNumber(block.getId(), normalizedNumber)) {
            throw new ConflictException("Flat number '" + normalizedNumber + "' already exists in " + block.getName());
        }

        Flat flat = Flat.builder()
                .block(block)
                .flatNumber(normalizedNumber)
                .floorNumber(request.floorNumber())
                .flatType(request.flatType())
                .status(FlatStatus.VACANT)
                .build();

        Flat savedFlat = flatRepository.save(flat);
        log.info("Created flat id={} number={} in block={}", savedFlat.getId(), savedFlat.getFlatNumber(), block.getName());

        return FlatResponse.fromEntity(savedFlat);
    }

    @Override
    @Transactional
    public FlatResponse updateFlat(Long id, UpdateFlatRequest request) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", id));

        flat.setFloorNumber(request.floorNumber());
        flat.setFlatType(request.flatType());
        flat.setStatus(request.status());

        Flat updatedFlat = flatRepository.save(flat);
        log.info("Updated flat id={} number={}", updatedFlat.getId(), updatedFlat.getFlatNumber());

        return FlatResponse.fromEntity(updatedFlat);
    }

    @Override
    @Transactional(readOnly = true)
    public FlatResponse getFlatById(Long id) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", id));
        return FlatResponse.fromEntity(flat);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlatResponse> getAllFlats(Long blockId, FlatType flatType, FlatStatus status, Pageable pageable) {
        Page<Flat> flats;

        if (blockId != null && status != null) {
            flats = flatRepository.findByBlockIdAndStatus(blockId, status, pageable);
        } else if (blockId != null) {
            flats = flatRepository.findByBlockId(blockId, pageable);
        } else if (status != null) {
            flats = flatRepository.findByStatus(status, pageable);
        } else if (flatType != null) {
            flats = flatRepository.findByFlatType(flatType, pageable);
        } else {
            flats = flatRepository.findAll(pageable);
        }

        return flats.map(FlatResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlatResponse> getFlatsByBlockId(Long blockId) {
        if (!blockRepository.existsById(blockId)) {
            throw new ResourceNotFoundException("Block", "id", blockId);
        }
        return flatRepository.findByBlockId(blockId).stream()
                .map(FlatResponse::fromEntity)
                .toList();
    }
}
