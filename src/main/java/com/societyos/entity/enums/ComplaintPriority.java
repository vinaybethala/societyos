package com.societyos.entity.enums;

public enum ComplaintPriority {
    LOW(72),
    MEDIUM(24),
    HIGH(4),
    EMERGENCY(2);

    private final int slaHours;

    ComplaintPriority(int slaHours) {
        this.slaHours = slaHours;
    }

    public int getSlaHours() {
        return slaHours;
    }
}

