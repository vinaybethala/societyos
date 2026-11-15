package com.societyos.entity;

import com.societyos.entity.enums.RoleType;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private RoleType name;

    @Column(name = "description")
    private String description;

    public Role() {
    }

    public Role(RoleType name, String description) {
        this.name = name;
        this.description = description;
    }

    public static RoleBuilder builder() {
        return new RoleBuilder();
    }

    public static class RoleBuilder {
        private RoleType name;
        private String description;

        public RoleBuilder name(RoleType name) {
            this.name = name;
            return this;
        }

        public RoleBuilder description(String description) {
            this.description = description;
            return this;
        }

        public Role build() {
            return new Role(name, description);
        }
    }

    public RoleType getName() {
        return name;
    }

    public void setName(RoleType name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
