package com.societyos.entity;

import com.societyos.entity.enums.FlatType;
import com.societyos.entity.enums.FlatStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "flats", uniqueConstraints = {
        @UniqueConstraint(name = "uk_flats_block_number", columnNames = {"block_id", "flat_number"})
})
public class Flat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Column(name = "flat_number", nullable = false, length = 20)
    private String flatNumber;

    @Column(name = "floor_number", nullable = false)
    private int floorNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "flat_type", nullable = false, length = 30)
    private FlatType flatType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FlatStatus status = FlatStatus.VACANT;

    public Flat() {
    }

    public Flat(Block block, String flatNumber, int floorNumber, FlatType flatType, FlatStatus status) {
        this.block = block;
        this.flatNumber = flatNumber;
        this.floorNumber = floorNumber;
        this.flatType = flatType;
        this.status = status != null ? status : FlatStatus.VACANT;
    }

    public static FlatBuilder builder() {
        return new FlatBuilder();
    }

    public static class FlatBuilder {
        private Block block;
        private String flatNumber;
        private int floorNumber;
        private FlatType flatType;
        private FlatStatus status = FlatStatus.VACANT;

        public FlatBuilder block(Block block) {
            this.block = block;
            return this;
        }

        public FlatBuilder flatNumber(String flatNumber) {
            this.flatNumber = flatNumber;
            return this;
        }

        public FlatBuilder floorNumber(int floorNumber) {
            this.floorNumber = floorNumber;
            return this;
        }

        public FlatBuilder flatType(FlatType flatType) {
            this.flatType = flatType;
            return this;
        }

        public FlatBuilder status(FlatStatus status) {
            this.status = status;
            return this;
        }

        public Flat build() {
            return new Flat(block, flatNumber, floorNumber, flatType, status);
        }
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public FlatType getFlatType() {
        return flatType;
    }

    public void setFlatType(FlatType flatType) {
        this.flatType = flatType;
    }

    public FlatStatus getStatus() {
        return status;
    }

    public void setStatus(FlatStatus status) {
        this.status = status;
    }
}

