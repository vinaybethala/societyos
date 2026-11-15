package com.societyos.service.impl;

import com.societyos.dto.request.CreateBlockRequest;
import com.societyos.dto.request.UpdateBlockRequest;
import com.societyos.dto.response.BlockResponse;
import com.societyos.entity.Block;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.BlockRepository;
import com.societyos.repository.FlatRepository;
import com.societyos.service.BlockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlockServiceImpl implements BlockService {

    private static final Logger log = LoggerFactory.getLogger(BlockServiceImpl.class);

    private final BlockRepository blockRepository;
    private final FlatRepository flatRepository;

    public BlockServiceImpl(BlockRepository blockRepository, FlatRepository flatRepository) {
        this.blockRepository = blockRepository;
        this.flatRepository = flatRepository;
    }

    @Override
    @Transactional
    public BlockResponse createBlock(CreateBlockRequest request) {
        String trimmedName = request.name().trim();
        if (blockRepository.existsByName(trimmedName)) {
            throw new ConflictException("Block with name '" + trimmedName + "' already exists");
        }

        Block block = new Block(trimmedName, request.description(), request.totalFloors());
        Block savedBlock = blockRepository.save(block);
        log.info("Created block id={} name={}", savedBlock.getId(), savedBlock.getName());

        return BlockResponse.fromEntity(savedBlock);
    }

    @Override
    @Transactional
    public BlockResponse updateBlock(Long id, UpdateBlockRequest request) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));

        String trimmedName = request.name().trim();
        blockRepository.findByName(trimmedName).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Another block with name '" + trimmedName + "' already exists");
            }
        });

        block.setName(trimmedName);
        block.setDescription(request.description());
        block.setTotalFloors(request.totalFloors());

        Block updatedBlock = blockRepository.save(block);
        log.info("Updated block id={} name={}", updatedBlock.getId(), updatedBlock.getName());

        return BlockResponse.fromEntity(updatedBlock);
    }

    @Override
    @Transactional(readOnly = true)
    public BlockResponse getBlockById(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));
        return BlockResponse.fromEntity(block);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockResponse> getAllBlocks() {
        return blockRepository.findAll().stream()
                .map(BlockResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteBlock(Long id) {
        Block block = blockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Block", "id", id));

        long flatsCount = flatRepository.countByBlockId(id);
        if (flatsCount > 0) {
            throw new ConflictException("Cannot delete block '" + block.getName() + "' because it has " + flatsCount + " associated flat(s)");
        }

        blockRepository.delete(block);
        log.info("Deleted block id={} name={}", id, block.getName());
    }
}

