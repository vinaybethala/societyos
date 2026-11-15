package com.societyos.service;

import com.societyos.dto.request.CreateVendorRequest;
import com.societyos.dto.response.VendorResponse;
import com.societyos.entity.Vendor;
import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import com.societyos.exception.ConflictException;
import com.societyos.repository.VendorRepository;
import com.societyos.service.impl.VendorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {

    @Mock
    private VendorRepository vendorRepository;

    @InjectMocks
    private VendorServiceImpl vendorService;

    private Vendor testVendor;

    @BeforeEach
    void setUp() {
        testVendor = new Vendor("Ramesh Plumbing", VendorServiceType.PLUMBER,
                "+919876543210", "ramesh@example.com", VendorStatus.ACTIVE, BigDecimal.ZERO, 0);
        testVendor.setId(10L);
    }

    @Test
    @DisplayName("Should create vendor with initial jobs=0 and rating=0.00")
    void shouldCreateVendorSuccessfully() {
        CreateVendorRequest request = new CreateVendorRequest(
                "Ramesh Plumbing", VendorServiceType.PLUMBER, "+919876543210", "ramesh@example.com");

        when(vendorRepository.existsByPhone("+919876543210")).thenReturn(false);
        when(vendorRepository.save(any(Vendor.class))).thenReturn(testVendor);

        VendorResponse response = vendorService.createVendor(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.serviceType()).isEqualTo(VendorServiceType.PLUMBER);
        assertThat(response.totalJobsCompleted()).isEqualTo(0);
        assertThat(response.averageRating()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should throw ConflictException on duplicate vendor phone")
    void shouldThrowOnDuplicatePhone() {
        CreateVendorRequest request = new CreateVendorRequest(
                "Ramesh Plumbing", VendorServiceType.PLUMBER, "+919876543210", "ramesh@example.com");

        when(vendorRepository.existsByPhone("+919876543210")).thenReturn(true);

        assertThatThrownBy(() -> vendorService.createVendor(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(vendorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should soft-delete vendor by updating status to INACTIVE")
    void shouldSoftDeleteVendor() {
        when(vendorRepository.findById(10L)).thenReturn(Optional.of(testVendor));

        vendorService.deleteVendor(10L);

        assertThat(testVendor.getStatus()).isEqualTo(VendorStatus.INACTIVE);
        verify(vendorRepository, times(1)).save(testVendor);
    }
}

