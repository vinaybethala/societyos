package com.societyos.entity;

import com.societyos.entity.enums.VendorServiceType;
import com.societyos.entity.enums.VendorStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "vendors")
public class Vendor extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private VendorServiceType serviceType;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private VendorStatus status = VendorStatus.ACTIVE;

    @Column(name = "average_rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "total_jobs_completed", nullable = false)
    private int totalJobsCompleted = 0;

    public Vendor() {
    }

    public Vendor(String name, VendorServiceType serviceType, String phone, String email,
                  VendorStatus status, BigDecimal averageRating, int totalJobsCompleted) {
        this.name = name;
        this.serviceType = serviceType;
        this.phone = phone;
        this.email = email;
        this.status = status != null ? status : VendorStatus.ACTIVE;
        this.averageRating = averageRating != null ? averageRating : BigDecimal.ZERO;
        this.totalJobsCompleted = totalJobsCompleted;
    }

    public static VendorBuilder builder() {
        return new VendorBuilder();
    }

    public static class VendorBuilder {
        private String name;
        private VendorServiceType serviceType;
        private String phone;
        private String email;
        private VendorStatus status = VendorStatus.ACTIVE;
        private BigDecimal averageRating = BigDecimal.ZERO;
        private int totalJobsCompleted = 0;

        public VendorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public VendorBuilder serviceType(VendorServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public VendorBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public VendorBuilder email(String email) {
            this.email = email;
            return this;
        }

        public VendorBuilder status(VendorStatus status) {
            this.status = status;
            return this;
        }

        public VendorBuilder averageRating(BigDecimal averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public VendorBuilder totalJobsCompleted(int totalJobsCompleted) {
            this.totalJobsCompleted = totalJobsCompleted;
            return this;
        }

        public Vendor build() {
            return new Vendor(name, serviceType, phone, email, status, averageRating, totalJobsCompleted);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VendorServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(VendorServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public VendorStatus getStatus() {
        return status;
    }

    public void setStatus(VendorStatus status) {
        this.status = status;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalJobsCompleted() {
        return totalJobsCompleted;
    }

    public void setTotalJobsCompleted(int totalJobsCompleted) {
        this.totalJobsCompleted = totalJobsCompleted;
    }
}
