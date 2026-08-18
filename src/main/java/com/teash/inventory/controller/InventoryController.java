/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.controller;

/**
 *
 * @author hp
 */

/**
 * Test Telegram connection
 */


import com.teash.inventory.entity.*;
import com.teash.inventory.service.InventoryService;
import com.teash.inventory.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private NotificationService notificationService;
    
    // ========== MATERIALS ==========
    
    @GetMapping("/materials")
    public ResponseEntity<List<RawMaterial>> getAllMaterials() {
        return ResponseEntity.ok(inventoryService.getAllMaterials());
    }
    
    @PostMapping("/materials/receive")
    public ResponseEntity<?> receiveMaterials(
            @RequestParam Long materialId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) BigDecimal costPerUnit,
            @RequestParam(required = false) String supplier) {
        try {
            RawMaterial result = inventoryService.receiveMaterials(materialId, quantity, costPerUnit, supplier);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/materials/{materialId}/threshold")
    public ResponseEntity<?> updateThreshold(
            @PathVariable Long materialId,
            @RequestParam Integer threshold) {
        try {
            RawMaterial result = inventoryService.updateThreshold(materialId, threshold);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/materials/{materialName}/quantity")
    public ResponseEntity<?> updateMaterialQuantity(
            @PathVariable String materialName,
            @RequestParam Integer newQuantity) {
        try {
            RawMaterial result = inventoryService.updateMaterialQuantity(materialName, newQuantity);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ========== SALES ==========
    
    @PostMapping("/sales/sell")
    public ResponseEntity<?> sellPacks(
            @RequestParam Long productId,
            @RequestParam Integer packs,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) BigDecimal salePrice) {
        try {
            FinishedGoods result = inventoryService.sellPacks(productId, packs, customerName, salePrice);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "💰 Sold " + packs + " product(s)");
            response.put("remainingStock", result.getQuantityPacks());
            
            // Check for alerts after sale
            notificationService.checkAndSendAlerts();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ========== DASHBOARD ==========
    
    @GetMapping("/inventory/status")
    public ResponseEntity<Map<String, Object>> getInventoryStatus() {
        return ResponseEntity.ok(inventoryService.getDashboardStatus());
    }
    
    @GetMapping("/inventory/finished")
    public ResponseEntity<List<FinishedGoods>> getFinishedGoods() {
        return ResponseEntity.ok(inventoryService.getAllFinishedGoods());
    }
    
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }
    
    /**
     * Get all product names only (for dropdowns)
     */
    @GetMapping("/products/names")
    public ResponseEntity<List<String>> getProductNames() {
        List<Product> products = inventoryService.getAllProducts();
        List<String> names = products.stream()
            .map(Product::getName)
            .collect(Collectors.toList());
        return ResponseEntity.ok(names);
    }
    
    // ========== NOTIFICATIONS ==========
    
    @PostMapping("/notifications/check")
    public ResponseEntity<Map<String, Object>> manualStockCheck() {
        notificationService.checkAndSendAlerts();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Stock check completed. Alerts sent if needed.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/notifications/history")
    public ResponseEntity<List<Notification>> getNotificationHistory() {
        return ResponseEntity.ok(inventoryService.getRecentNotifications());
    }
    
    // ========== HISTORY ==========
    
    @GetMapping("/production/history")
    public ResponseEntity<List<ProductionRun>> getProductionHistory() {
        return ResponseEntity.ok(inventoryService.getProductionHistory());
    }
    
    @GetMapping("/sales/history")
    public ResponseEntity<List<Sale>> getSalesHistory() {
        return ResponseEntity.ok(inventoryService.getSalesHistory());
    }
    
    // ========== TELEGRAM TEST ENDPOINTS ==========
    
    @GetMapping("/telegram/test")
    public ResponseEntity<Map<String, Object>> testTelegram() {
        try {
            notificationService.testTelegramConnection();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Telegram test message sent! Check your phone!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "❌ Failed to send test: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/telegram/send")
    public ResponseEntity<Map<String, Object>> sendTelegramMessage(
            @RequestParam String title,
            @RequestParam String message) {
        try {
            notificationService.sendTelegramCustomAlert(title, message);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Telegram message sent!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "❌ Failed to send: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}