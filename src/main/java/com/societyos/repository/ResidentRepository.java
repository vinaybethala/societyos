package com.societyos.repository;

import com.societyos.entity.Resident;
import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    List<Resident> findByFlatId(Long flatId);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    List<Resident> findByFlatIdAndStatus(Long flatId, ResidentStatus status);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    List<Resident> findByUserIdAndStatus(Long userId, ResidentStatus status);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Optional<Resident> findFirstByUserIdAndStatus(Long userId, ResidentStatus status);

    boolean existsByFlatIdAndStatus(Long flatId, ResidentStatus status);

    @Override
    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Page<Resident> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Page<Resident> findByFlatId(Long flatId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Page<Resident> findByStatus(ResidentStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Page<Resident> findByResidentType(ResidentType residentType, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "flat", "flat.block"})
    Page<Resident> findByFlatIdAndStatus(Long flatId, ResidentStatus status, Pageable pageable);
}
