package com.societyos.service;

import com.societyos.dto.request.CreateBlockRequest;
import com.societyos.dto.request.UpdateBlockRequest;
import com.societyos.dto.response.BlockResponse;

import java.util.List;

public interface BlockService {
    BlockResponse createBlock(CreateBlockRequest request);
    BlockResponse updateBlock(Long id, UpdateBlockRequest request);
    BlockResponse getBlockById(Long id);
    List<BlockResponse> getAllBlocks();
    void deleteBlock(Long id);
}
