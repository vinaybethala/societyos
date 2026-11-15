package com.societyos.entity;

import com.societyos.entity.enums.ResidentStatus;
import com.societyos.entity.enums.ResidentType;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "residents")
public class Resident extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id", nullable = false)
    private Flat flat;

    @Enumerated(EnumType.STRING)
    @Column(name = "resident_type", nullable = false, length = 30)
    private ResidentType residentType;

    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;

    @Column(name = "move_out_date")
    private LocalDate moveOutDate;

    @Column(name = "is_primary_contact", nullable = false)
    private boolean isPrimaryContact = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ResidentStatus status = ResidentStatus.ACTIVE;

    public Resident() {
    }

    public Resident(User user, Flat flat, ResidentType residentType, LocalDate moveInDate,
                    LocalDate moveOutDate, boolean isPrimaryContact, ResidentStatus status) {
        this.user = user;
        this.flat = flat;
        this.residentType = residentType;
        this.moveInDate = moveInDate;
        this.moveOutDate = moveOutDate;
        this.isPrimaryContact = isPrimaryContact;
        this.status = status != null ? status : ResidentStatus.ACTIVE;
    }

    public static ResidentBuilder builder() {
        return new ResidentBuilder();
    }

    public static class ResidentBuilder {
        private User user;
        private Flat flat;
        private ResidentType residentType;
        private LocalDate moveInDate;
        private LocalDate moveOutDate;
        private boolean isPrimaryContact = false;
        private ResidentStatus status = ResidentStatus.ACTIVE;

        public ResidentBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ResidentBuilder flat(Flat flat) {
            this.flat = flat;
            return this;
        }

        public ResidentBuilder residentType(ResidentType residentType) {
            this.residentType = residentType;
            return this;
        }

        public ResidentBuilder moveInDate(LocalDate moveInDate) {
            this.moveInDate = moveInDate;
            return this;
        }

        public ResidentBuilder moveOutDate(LocalDate moveOutDate) {
            this.moveOutDate = moveOutDate;
            return this;
        }

        public ResidentBuilder isPrimaryContact(boolean isPrimaryContact) {
            this.isPrimaryContact = isPrimaryContact;
            return this;
        }

        public ResidentBuilder status(ResidentStatus status) {
            this.status = status;
            return this;
        }

        public Resident build() {
            return new Resident(user, flat, residentType, moveInDate, moveOutDate, isPrimaryContact, status);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Flat getFlat() {
        return flat;
    }

    public void setFlat(Flat flat) {
        this.flat = flat;
    }

    public ResidentType getResidentType() {
        return residentType;
    }

    public void setResidentType(ResidentType residentType) {
        this.residentType = residentType;
    }

    public LocalDate getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }

    public LocalDate getMoveOutDate() {
        return moveOutDate;
    }

    public void setMoveOutDate(LocalDate moveOutDate) {
        this.moveOutDate = moveOutDate;
    }

    public boolean isPrimaryContact() {
        return isPrimaryContact;
    }

    public void setPrimaryContact(boolean primaryContact) {
        isPrimaryContact = primaryContact;
    }

    public ResidentStatus getStatus() {
        return status;
    }

    public void setStatus(ResidentStatus status) {
        this.status = status;
    }
}
