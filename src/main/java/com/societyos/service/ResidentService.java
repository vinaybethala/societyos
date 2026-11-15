package com.societyos.service;

import com.societyos.dto.request.AssignResidentRequest;
import com.societyos.dto.request.MoveOutRequest;
import com.societyos.dto.response.ResidentResponse;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ResidentService {
    ResidentResponse assignResident(AssignResidentRequest request);
    ResidentResponse recordMoveOut(Long residentId, MoveOutRequest request);
    ResidentResponse getResidentById(Long id);
    List<ResidentResponse> getResidentsByFlat(Long flatId);
    List<ResidentResponse> getMyFlats();
    Page<ResidentResponse> getAllResidents(Long flatId, ResidentType residentType, ResidentStatus status, Pageable pageable);
}
