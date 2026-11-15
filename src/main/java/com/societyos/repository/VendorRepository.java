package com.societyos.repository;

import com.societyos.entity.Vendor;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByPhone(String phone);

    boolean existsByPhone(String phone);

    Page<Vendor> findByServiceType(VendorServiceType serviceType, Pageable pageable);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    Page<Vendor> findByServiceTypeAndStatus(VendorServiceType serviceType, VendorStatus status, Pageable pageable);
}
