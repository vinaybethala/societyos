package com.societyos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "blocks")
public class Block extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "total_floors", nullable = false)
    private int totalFloors = 1;

    public Block() {
    }

    public Block(String name, String description, int totalFloors) {
        this.name = name;
        this.description = description;
        this.totalFloors = totalFloors;
    }

    public static BlockBuilder builder() {
        return new BlockBuilder();
    }

    public static class BlockBuilder {
        private String name;
        private String description;
        private int totalFloors = 1;

        public BlockBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BlockBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BlockBuilder totalFloors(int totalFloors) {
            this.totalFloors = totalFloors;
            return this;
        }

        public Block build() {
            return new Block(name, description, totalFloors);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(int totalFloors) {
        this.totalFloors = totalFloors;
    }
}
