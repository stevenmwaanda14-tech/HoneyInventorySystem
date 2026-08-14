/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.entity;

/**
 *
 * @author hp
 */


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_materials")
public class RawMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50, unique = true)
    private String name;
    
    @Column(name = "unit_type", length = 20)
    private String unitType = "pieces";
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    @Column(name = "min_threshold")
    private Integer minThreshold = 50;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    // Constructors
    public RawMaterial() {}
    
    public RawMaterial(String name, Integer minThreshold) {
        this.name = name;
        this.minThreshold = minThreshold;
        this.quantity = 0;
        this.unitType = "pieces";
    }
    
    // Business methods
    public void addQuantity(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantity += amount;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void subtractQuantity(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException(
                String.format("Not enough %s. Available: %d, Need: %d", 
                    this.name, this.quantity, amount)
            );
        }
        this.quantity -= amount;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public boolean isBelowThreshold() {
        return this.quantity <= this.minThreshold;
    }
    
    public boolean isOutOfStock() {
        return this.quantity == 0;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public Integer getMinThreshold() { return minThreshold; }
    public void setMinThreshold(Integer minThreshold) { this.minThreshold = minThreshold; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}