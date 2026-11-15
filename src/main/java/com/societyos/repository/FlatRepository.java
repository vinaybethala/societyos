package com.societyos.repository;

import com.societyos.entity.Flat;
import com.societyos.entity.enums.FlatStatus;
import com.societyos.entity.enums.FlatType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlatRepository extends JpaRepository<Flat, Long> {

    @EntityGraph(attributePaths = {"block"})
    Optional<Flat> findByBlockIdAndFlatNumber(Long blockId, String flatNumber);

    boolean existsByBlockIdAndFlatNumber(Long blockId, String flatNumber);

    long countByBlockId(Long blockId);

    @EntityGraph(attributePaths = {"block"})
    List<Flat> findByBlockId(Long blockId);

    @Override
    @EntityGraph(attributePaths = {"block"})
    Page<Flat> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"block"})
    Page<Flat> findByBlockId(Long blockId, Pageable pageable);

    @EntityGraph(attributePaths = {"block"})
    Page<Flat> findByStatus(FlatStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"block"})
    Page<Flat> findByFlatType(FlatType flatType, Pageable pageable);

    @EntityGraph(attributePaths = {"block"})
    Page<Flat> findByBlockIdAndStatus(Long blockId, FlatStatus status, Pageable pageable);
}
