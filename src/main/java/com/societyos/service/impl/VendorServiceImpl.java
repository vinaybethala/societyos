package com.societyos.service.impl;

import com.societyos.dto.request.CreateVendorRequest;
import com.societyos.dto.request.UpdateVendorRequest;
import com.societyos.dto.response.VendorResponse;
import com.societyos.entity.Vendor;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import com.societyos.exception.ConflictException;
import com.societyos.exception.ResourceNotFoundException;
import com.societyos.repository.VendorRepository;
import com.societyos.service.VendorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class VendorServiceImpl implements VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorServiceImpl.class);

    private final VendorRepository vendorRepository;

    public VendorServiceImpl(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional
    public VendorResponse createVendor(CreateVendorRequest request) {
        String trimmedPhone = request.phone().trim();
        if (vendorRepository.existsByPhone(trimmedPhone)) {
            throw new ConflictException("Vendor with phone number '" + trimmedPhone + "' already exists");
        }

        Vendor vendor = Vendor.builder()
                .name(request.name().trim())
                .serviceType(request.serviceType())
                .phone(trimmedPhone)
                .email(request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null)
                .status(VendorStatus.ACTIVE)
                .averageRating(BigDecimal.ZERO)
                .totalJobsCompleted(0)
                .build();

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Registered new vendor id={} name={} service={}", savedVendor.getId(), savedVendor.getName(), savedVendor.getServiceType());

        return VendorResponse.fromEntity(savedVendor);
    }

    @Override
    @Transactional
    public VendorResponse updateVendor(Long id, UpdateVendorRequest request) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));

        String trimmedPhone = request.phone().trim();
        vendorRepository.findByPhone(trimmedPhone).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Another vendor with phone number '" + trimmedPhone + "' already exists");
            }
        });

        vendor.setName(request.name().trim());
        vendor.setServiceType(request.serviceType());
        vendor.setPhone(trimmedPhone);
        vendor.setEmail(request.email() != null && !request.email().isBlank() ? request.email().trim().toLowerCase() : null);
        vendor.setStatus(request.status());

        Vendor updatedVendor = vendorRepository.save(vendor);
        log.info("Updated vendor id={} name={}", updatedVendor.getId(), updatedVendor.getName());

        return VendorResponse.fromEntity(updatedVendor);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));
        return VendorResponse.fromEntity(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> getAllVendors(VendorServiceType serviceType, VendorStatus status, Pageable pageable) {
        Page<Vendor> vendors;
        if (serviceType != null && status != null) {
            vendors = vendorRepository.findByServiceTypeAndStatus(serviceType, status, pageable);
        } else if (serviceType != null) {
            vendors = vendorRepository.findByServiceType(serviceType, pageable);
        } else if (status != null) {
            vendors = vendorRepository.findByStatus(status, pageable);
        } else {
            vendors = vendorRepository.findAll(pageable);
        }
        return vendors.map(VendorResponse::fromEntity);
    }

    @Override
    @Transactional
    public void deleteVendor(Long id) {
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", id));

        vendor.setStatus(VendorStatus.INACTIVE);
        vendorRepository.save(vendor);
        log.info("Deactivated (soft-deleted) vendor id={} name={}", id, vendor.getName());
    }
}
