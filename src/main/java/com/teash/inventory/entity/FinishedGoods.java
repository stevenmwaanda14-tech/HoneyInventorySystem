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
@Table(name = "finished_goods")
public class FinishedGoods {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity_packs")
    private Integer quantityPacks = 0;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();
    
    // Constructors
    public FinishedGoods() {}
    
    public FinishedGoods(Product product, Integer quantityPacks) {
        this.product = product;
        this.quantityPacks = quantityPacks;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Business methods
    public void addPacks(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.quantityPacks += amount;
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void subtractPacks(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.quantityPacks < amount) {
            throw new IllegalArgumentException(
                String.format("Not enough packs. Available: %d, Need: %d", 
                    this.quantityPacks, amount)
            );
        }
        this.quantityPacks -= amount;
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public Integer getQuantityPacks() { return quantityPacks; }
    public void setQuantityPacks(Integer quantityPacks) { this.quantityPacks = quantityPacks; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}