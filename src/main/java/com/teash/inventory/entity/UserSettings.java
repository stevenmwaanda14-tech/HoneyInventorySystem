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
@Table(name = "user_settings")
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String userEmail;
    
    @Column(length = 20)
    private String phoneNumber;
    
    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;
    
    @Column(name = "notify_low_stock")
    private Boolean notifyLowStock = true;
    
    @Column(name = "notify_critical_stock")
    private Boolean notifyCriticalStock = true;
    
    @Column(name = "notify_production")
    private Boolean notifyProduction = true;
    
    @Column(name = "notify_sales")
    private Boolean notifySales = true;
    
    @Column(name = "notify_daily_summary")
    private Boolean notifyDailySummary = false;
    
    @Column(name = "low_threshold_multiplier")
    private Double lowThresholdMultiplier = 1.0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // ========== CONSTRUCTORS ==========
    
    public UserSettings() {}
    
    public UserSettings(String userEmail, String telegramChatId) {
        this.userEmail = userEmail;
        this.telegramChatId = telegramChatId;
        this.notifyLowStock = true;
        this.notifyCriticalStock = true;
        this.notifyProduction = true;
        this.notifySales = true;
        this.notifyDailySummary = false;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { 
        this.userEmail = userEmail; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { 
        this.phoneNumber = phoneNumber; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { 
        this.telegramChatId = telegramChatId; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getNotifyLowStock() { return notifyLowStock; }
    public void setNotifyLowStock(Boolean notifyLowStock) { 
        this.notifyLowStock = notifyLowStock; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getNotifyCriticalStock() { return notifyCriticalStock; }
    public void setNotifyCriticalStock(Boolean notifyCriticalStock) { 
        this.notifyCriticalStock = notifyCriticalStock; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getNotifyProduction() { return notifyProduction; }
    public void setNotifyProduction(Boolean notifyProduction) { 
        this.notifyProduction = notifyProduction; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getNotifySales() { return notifySales; }
    public void setNotifySales(Boolean notifySales) { 
        this.notifySales = notifySales; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Boolean getNotifyDailySummary() { return notifyDailySummary; }
    public void setNotifyDailySummary(Boolean notifyDailySummary) { 
        this.notifyDailySummary = notifyDailySummary; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public Double getLowThresholdMultiplier() { return lowThresholdMultiplier; }
    public void setLowThresholdMultiplier(Double lowThresholdMultiplier) { 
        this.lowThresholdMultiplier = lowThresholdMultiplier; 
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
        this.updatedAt = LocalDateTime.now();
    }
    
    // ========== BUSINESS METHODS ==========
    
    public boolean shouldNotifyLowStock() {
        return isActive && notifyLowStock != null && notifyLowStock;
    }
    
    public boolean shouldNotifyCriticalStock() {
        return isActive && notifyCriticalStock != null && notifyCriticalStock;
    }
    
    public boolean shouldNotifyProduction() {
        return isActive && notifyProduction != null && notifyProduction;
    }
    
    public boolean shouldNotifySales() {
        return isActive && notifySales != null && notifySales;
    }
    
    public boolean shouldNotifyDailySummary() {
        return isActive && notifyDailySummary != null && notifyDailySummary;
    }
}