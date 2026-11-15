package com.societyos.service;

import com.societyos.dto.request.CreateFlatRequest;
import com.societyos.dto.response.FlatResponse;
import com.societyos.entity.Block;
import com.societyos.entity.Flat;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import com.societyos.exception.ConflictException;
import com.societyos.repository.BlockRepository;
import com.societyos.repository.FlatRepository;
import com.societyos.service.impl.FlatServiceImpl;
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
class FlatServiceTest {

    @Mock
    private FlatRepository flatRepository;

    @Mock
    private BlockRepository blockRepository;

    @InjectMocks
    private FlatServiceImpl flatService;

    private Block testBlock;
    private Flat testFlat;

    @BeforeEach
    void setUp() {
        testBlock = new Block("Tower B", "Phase 2", 12);
        testBlock.setId(5L);

        testFlat = new Flat(testBlock, "B-201", 2, FlatType.TWO_BHK, FlatStatus.VACANT);
        testFlat.setId(20L);
    }

    @Test
    @DisplayName("Should create flat successfully with initial status VACANT")
    void shouldCreateFlatSuccessfully() {
        CreateFlatRequest request = new CreateFlatRequest(5L, "B-201", 2, FlatType.TWO_BHK);

        when(blockRepository.findById(5L)).thenReturn(Optional.of(testBlock));
        when(flatRepository.existsByBlockIdAndFlatNumber(5L, "B-201")).thenReturn(false);
        when(flatRepository.save(any(Flat.class))).thenReturn(testFlat);

        FlatResponse response = flatService.createFlat(request);

        assertThat(response).isNotNull();
        assertThat(response.flatNumber()).isEqualTo("B-201");
        assertThat(response.status()).isEqualTo(FlatStatus.VACANT);
        assertThat(response.blockName()).isEqualTo("Tower B");
    }

    @Test
    @DisplayName("Should throw ConflictException on duplicate flat number within same block")
    void shouldThrowOnDuplicateFlatInBlock() {
        CreateFlatRequest request = new CreateFlatRequest(5L, "B-201", 2, FlatType.TWO_BHK);

        when(blockRepository.findById(5L)).thenReturn(Optional.of(testBlock));
        when(flatRepository.existsByBlockIdAndFlatNumber(5L, "B-201")).thenReturn(true);

        assertThatThrownBy(() -> flatService.createFlat(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(flatRepository, never()).save(any());
    }
}
