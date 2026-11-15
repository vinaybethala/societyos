package com.societyos.service;

import com.societyos.dto.request.CreateVendorRequest;
import com.societyos.dto.request.UpdateVendorRequest;
import com.societyos.dto.response.VendorResponse;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorService {
    VendorResponse createVendor(CreateVendorRequest request);
    VendorResponse updateVendor(Long id, UpdateVendorRequest request);
    VendorResponse getVendorById(Long id);
    Page<VendorResponse> getAllVendors(VendorServiceType serviceType, VendorStatus status, Pageable pageable);
    void deleteVendor(Long id);
}
