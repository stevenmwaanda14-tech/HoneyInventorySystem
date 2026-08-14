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
@Table(name = "production_runs")
public class ProductionRun {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "packs_produced", nullable = false)
    private Integer packsProduced;
    
    @Column(name = "jars_used", nullable = false)
    private Integer jarsUsed;
    
    @Column(name = "stickers_used", nullable = false)
    private Integer stickersUsed;
    
    @Column(name = "boxes_used", nullable = false)
    private Integer boxesUsed;
    
    @Column(name = "produced_by", length = 50)
    private String producedBy;
    
    @Column(name = "production_date")
    private LocalDateTime productionDate = LocalDateTime.now();
    
    // Constructors
    public ProductionRun() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public Integer getPacksProduced() { return packsProduced; }
    public void setPacksProduced(Integer packsProduced) { this.packsProduced = packsProduced; }
    
    public Integer getJarsUsed() { return jarsUsed; }
    public void setJarsUsed(Integer jarsUsed) { this.jarsUsed = jarsUsed; }
    
    public Integer getStickersUsed() { return stickersUsed; }
    public void setStickersUsed(Integer stickersUsed) { this.stickersUsed = stickersUsed; }
    
    public Integer getBoxesUsed() { return boxesUsed; }
    public void setBoxesUsed(Integer boxesUsed) { this.boxesUsed = boxesUsed; }
    
    public String getProducedBy() { return producedBy; }
    public void setProducedBy(String producedBy) { this.producedBy = producedBy; }
    
    public LocalDateTime getProductionDate() { return productionDate; }
    public void setProductionDate(LocalDateTime productionDate) { this.productionDate = productionDate; }
}