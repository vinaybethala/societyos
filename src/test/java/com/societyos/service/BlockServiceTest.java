package com.societyos.service;

import com.societyos.dto.request.CreateBlockRequest;
import com.societyos.dto.response.BlockResponse;
import com.societyos.entity.Block;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.BlockRepository;
import com.societyos.repository.FlatRepository;
import com.societyos.service.impl.BlockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockServiceTest {

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private FlatRepository flatRepository;

    @InjectMocks
    private BlockServiceImpl blockService;

    private Block testBlock;

    @BeforeEach
    void setUp() {
        testBlock = new Block("Tower A", "Main Tower", 10);
        testBlock.setId(1L);
    }

    @Test
    @DisplayName("Should create block successfully")
    void shouldCreateBlockSuccessfully() {
        CreateBlockRequest request = new CreateBlockRequest("Tower A", "Main Tower", 10);

        when(blockRepository.existsByName("Tower A")).thenReturn(false);
        when(blockRepository.save(any(Block.class))).thenReturn(testBlock);

        BlockResponse response = blockService.createBlock(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Tower A");
        assertThat(response.totalFloors()).isEqualTo(10);
        verify(blockRepository, times(1)).save(any(Block.class));
    }

    @Test
    @DisplayName("Should throw ConflictException on duplicate block name")
    void shouldThrowOnDuplicateBlockName() {
        CreateBlockRequest request = new CreateBlockRequest("Tower A", "Main Tower", 10);

        when(blockRepository.existsByName("Tower A")).thenReturn(true);

        assertThatThrownBy(() -> blockService.createBlock(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(blockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete block if no flats are associated")
    void shouldDeleteBlockWhenEmpty() {
        when(blockRepository.findById(1L)).thenReturn(Optional.of(testBlock));
        when(flatRepository.countByBlockId(1L)).thenReturn(0L);

        blockService.deleteBlock(1L);

        verify(blockRepository, times(1)).delete(testBlock);
    }

    @Test
    @DisplayName("Should prevent deleting block containing flats")
    void shouldRejectDeleteWhenFlatsExist() {
        when(blockRepository.findById(1L)).thenReturn(Optional.of(testBlock));
        when(flatRepository.countByBlockId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> blockService.deleteBlock(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("associated flat(s)");

        verify(blockRepository, never()).delete(any());
    }
}
