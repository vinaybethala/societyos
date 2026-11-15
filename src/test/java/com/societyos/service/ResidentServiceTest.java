package com.societyos.service;

import com.societyos.dto.request.AssignResidentRequest;
import com.societyos.dto.request.MoveOutRequest;
import com.societyos.dto.response.ResidentResponse;
import com.societyos.entity.Block;
import com.societyos.entity.Flat;
import com.societyos.entity.Resident;
import com.societyos.entity.User;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import com.societyos.exception.BadRequestException;
import com.societyos.exception.ConflictException;
import com.societyos.repository.FlatRepository;
import com.societyos.repository.ResidentRepository;
import com.societyos.repository.UserRepository;
import com.societyos.service.impl.ResidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResidentServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private FlatRepository flatRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ResidentServiceImpl residentService;

    private User testUser;
    private Flat testFlat;
    private Resident testResident;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setUsername("resident_user");
        testUser.setEmail("resident@example.com");
        testUser.setFullName("Resident User");

        Block block = new Block("Block A", "Tower", 5);
        block.setId(1L);

        testFlat = new Flat(block, "A-101", 1, FlatType.TWO_BHK, FlatStatus.VACANT);
        testFlat.setId(50L);

        testResident = new Resident(testUser, testFlat, ResidentType.OWNER,
                LocalDate.of(2026, 1, 1), null, true, ResidentStatus.ACTIVE);
        testResident.setId(100L);
    }

    @Test
    @DisplayName("Should assign resident and update flat status to OCCUPIED")
    void shouldAssignResidentAndUpdateFlatStatus() {
        AssignResidentRequest request = new AssignResidentRequest(
                10L, 50L, ResidentType.OWNER, LocalDate.of(2026, 1, 1), true);

        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(flatRepository.findById(50L)).thenReturn(Optional.of(testFlat));
        when(residentRepository.findByFlatIdAndStatus(50L, ResidentStatus.ACTIVE))
                .thenReturn(Collections.emptyList());
        when(residentRepository.save(any(Resident.class))).thenReturn(testResident);

        ResidentResponse response = residentService.assignResident(request);

        assertThat(response).isNotNull();
        assertThat(response.flatId()).isEqualTo(50L);
        assertThat(response.residentType()).isEqualTo(ResidentType.OWNER);
        assertThat(testFlat.getStatus()).isEqualTo(FlatStatus.OCCUPIED);

        verify(flatRepository, times(1)).save(testFlat);
        verify(residentRepository, times(1)).save(any(Resident.class));
    }

    @Test
    @DisplayName("Should throw ConflictException if user is already an active resident of the flat")
    void shouldThrowWhenAlreadyActiveResident() {
        AssignResidentRequest request = new AssignResidentRequest(
                10L, 50L, ResidentType.OWNER, LocalDate.of(2026, 1, 1), true);

        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(flatRepository.findById(50L)).thenReturn(Optional.of(testFlat));
        when(residentRepository.findByFlatIdAndStatus(50L, ResidentStatus.ACTIVE))
                .thenReturn(List.of(testResident));

        assertThatThrownBy(() -> residentService.assignResident(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already an active resident");

        verify(residentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should record move-out and revert flat status to VACANT if no other active residents remain")
    void shouldRevertFlatToVacantOnSoleResidentMoveOut() {
        testFlat.setStatus(FlatStatus.OCCUPIED);
        MoveOutRequest request = new MoveOutRequest(LocalDate.of(2026, 11, 1));

        when(residentRepository.findById(100L)).thenReturn(Optional.of(testResident));
        when(residentRepository.save(any(Resident.class))).thenReturn(testResident);
        when(residentRepository.existsByFlatIdAndStatus(50L, ResidentStatus.ACTIVE)).thenReturn(false);

        ResidentResponse response = residentService.recordMoveOut(100L, request);

        assertThat(response).isNotNull();
        assertThat(testResident.getStatus()).isEqualTo(ResidentStatus.MOVED_OUT);
        assertThat(testResident.getMoveOutDate()).isEqualTo(LocalDate.of(2026, 11, 1));
        assertThat(testFlat.getStatus()).isEqualTo(FlatStatus.VACANT);

        verify(flatRepository, times(1)).save(testFlat);
    }

    @Test
    @DisplayName("Should reject move-out date prior to move-in date")
    void shouldRejectInvalidMoveOutDate() {
        MoveOutRequest request = new MoveOutRequest(LocalDate.of(2025, 12, 31)); // Move in was 2026-01-01

        when(residentRepository.findById(100L)).thenReturn(Optional.of(testResident));

        assertThatThrownBy(() -> residentService.recordMoveOut(100L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be before move-in date");

        verify(flatRepository, never()).save(any());
    }
}

