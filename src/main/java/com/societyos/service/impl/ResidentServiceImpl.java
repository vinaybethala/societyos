package com.societyos.service.impl;

import com.societyos.dto.request.AssignResidentRequest;
import com.societyos.dto.request.MoveOutRequest;
import com.societyos.dto.response.ResidentResponse;
import com.societyos.entity.Flat;
import com.societyos.entity.Resident;
import com.societyos.entity.User;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import com.societyos.exception.BadRequestException;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.FlatRepository;
import com.societyos.repository.ResidentRepository;
import com.societyos.repository.UserRepository;
import com.societyos.security.SecurityUtils;
import com.societyos.service.ResidentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResidentServiceImpl implements ResidentService {

    private static final Logger log = LoggerFactory.getLogger(ResidentServiceImpl.class);

    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;

    public ResidentServiceImpl(ResidentRepository residentRepository,
                               FlatRepository flatRepository,
                               UserRepository userRepository) {
        this.residentRepository = residentRepository;
        this.flatRepository = flatRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ResidentResponse assignResident(AssignResidentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.userId()));

        Flat flat = flatRepository.findById(request.flatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "id", request.flatId()));

        // Check if user is already an active resident of this flat
        boolean alreadyResident = residentRepository.findByFlatIdAndStatus(flat.getId(), ResidentStatus.ACTIVE)
                .stream()
                .anyMatch(r -> r.getUser().getId().equals(user.getId()));

        if (alreadyResident) {
            throw new ConflictException("User '" + user.getUsername() + "' is already an active resident of flat " + flat.getFlatNumber());
        }

        Resident resident = Resident.builder()
                .user(user)
                .flat(flat)
                .residentType(request.residentType())
                .moveInDate(request.moveInDate())
                .isPrimaryContact(request.isPrimaryContact())
                .status(ResidentStatus.ACTIVE)
                .build();

        Resident savedResident = residentRepository.save(resident);

        // Update flat occupancy status
        if (flat.getStatus() != FlatStatus.OCCUPIED) {
            flat.setStatus(FlatStatus.OCCUPIED);
            flatRepository.save(flat);
            log.info("Flat id={} number={} status updated to OCCUPIED", flat.getId(), flat.getFlatNumber());
        }

        log.info("Assigned user id={} to flat id={} as {}", user.getId(), flat.getId(), request.residentType());
        return ResidentResponse.fromEntity(savedResident);
    }

    @Override
    @Transactional
    public ResidentResponse recordMoveOut(Long residentId, MoveOutRequest request) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", residentId));

        if (resident.getStatus() == ResidentStatus.MOVED_OUT) {
            throw new BadRequestException("Resident has already moved out");
        }

        if (request.moveOutDate().isBefore(resident.getMoveInDate())) {
            throw new BadRequestException("Move-out date (" + request.moveOutDate() + ") cannot be before move-in date (" + resident.getMoveInDate() + ")");
        }

        resident.setStatus(ResidentStatus.MOVED_OUT);
        resident.setMoveOutDate(request.moveOutDate());
        Resident updatedResident = residentRepository.save(resident);

        // Check if any other active residents remain in the flat
        Flat flat = resident.getFlat();
        boolean hasRemainingActiveResidents = residentRepository.existsByFlatIdAndStatus(flat.getId(), ResidentStatus.ACTIVE);
        if (!hasRemainingActiveResidents) {
            flat.setStatus(FlatStatus.VACANT);
            flatRepository.save(flat);
            log.info("No active residents remain for flat id={} number={}. Status updated to VACANT.", flat.getId(), flat.getFlatNumber());
        }

        log.info("Recorded move out for resident id={} flat id={}", residentId, flat.getId());
        return ResidentResponse.fromEntity(updatedResident);
    }

    @Override
    @Transactional(readOnly = true)
    public ResidentResponse getResidentById(Long id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resident", "id", id));
        return ResidentResponse.fromEntity(resident);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentResponse> getResidentsByFlat(Long flatId) {
        if (!flatRepository.existsById(flatId)) {
            throw new ResourceNotFoundException("Flat", "id", flatId);
        }
        return residentRepository.findByFlatId(flatId).stream()
                .map(ResidentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResidentResponse> getMyFlats() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return residentRepository.findByUserIdAndStatus(currentUserId, ResidentStatus.ACTIVE).stream()
                .map(ResidentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResidentResponse> getAllResidents(Long flatId, ResidentType residentType, ResidentStatus status, Pageable pageable) {
        Page<Resident> page;
        if (flatId != null && status != null) {
            page = residentRepository.findByFlatIdAndStatus(flatId, status, pageable);
        } else if (flatId != null) {
            page = residentRepository.findByFlatId(flatId, pageable);
        } else if (status != null) {
            page = residentRepository.findByStatus(status, pageable);
        } else if (residentType != null) {
            page = residentRepository.findByResidentType(residentType, pageable);
        } else {
            page = residentRepository.findAll(pageable);
        }
        return page.map(ResidentResponse::fromEntity);
    }
}
