package com.teash.inventory.controller;

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
    
    // ========== SALES (LEGACY - KEPT FOR BACKWARD COMPATIBILITY) ==========
    
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
            
            notificationService.checkAndSendAlerts();
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ========== USED (NEW) WITH AUTO MATERIAL DEDUCTION ==========
    
    @PostMapping("/used/record")
    public ResponseEntity<?> recordUsed(@RequestBody Map<String, Object> usedData) {
        try {
            // Extract values with proper null handling
            Long productId = Long.parseLong(usedData.get("productId").toString());
            Integer packs = Integer.parseInt(usedData.get("packs").toString());
            
            String customerName = usedData.get("customerName") != null ? 
                usedData.get("customerName").toString() : "";
            
            Integer deductJars = 0;
            if (usedData.get("deductJars") != null) {
                deductJars = Integer.parseInt(usedData.get("deductJars").toString());
            }
            
            Integer deductStickers = 0;
            if (usedData.get("deductStickers") != null) {
                deductStickers = Integer.parseInt(usedData.get("deductStickers").toString());
            }
            
            Integer deductBoxes = 0;
            if (usedData.get("deductBoxes") != null) {
                deductBoxes = Integer.parseInt(usedData.get("deductBoxes").toString());
            }
            
            // Validate
            if (productId == null || productId <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Valid product ID is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (packs == null || packs <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Valid quantity is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Process the usage
            Map<String, Object> result = inventoryService.recordUsed(
                productId, packs, customerName, 
                deductJars, deductStickers, deductBoxes
            );
            
            return ResponseEntity.ok(result);
            
        } catch (NumberFormatException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid number format: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // ========== PRODUCT MANAGEMENT ==========
    
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }
    
    @GetMapping("/products/names")
    public ResponseEntity<List<String>> getProductNames() {
        List<Product> products = inventoryService.getAllProducts();
        List<String> names = products.stream()
            .map(Product::getName)
            .collect(Collectors.toList());
        return ResponseEntity.ok(names);
    }
    
    @PostMapping("/products/add")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> productData) {
        try {
            // Extract values with proper null handling
            String name = productData.get("name") != null ? 
                productData.get("name").toString().trim() : "";
            
            Integer initialStock = 0;
            if (productData.get("initialStock") != null) {
                try {
                    initialStock = Integer.parseInt(productData.get("initialStock").toString());
                } catch (NumberFormatException e) {
                    initialStock = 0;
                }
            }
            
            Integer jarsPerPack = 1;
            if (productData.get("jarsPerPack") != null) {
                try {
                    jarsPerPack = Integer.parseInt(productData.get("jarsPerPack").toString());
                } catch (NumberFormatException e) {
                    jarsPerPack = 1;
                }
            }
            
            // Validate
            if (name.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Product name is required");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Create product
            Product saved = inventoryService.addNewProduct(name, initialStock, jarsPerPack);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added successfully");
            response.put("product", saved);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        try {
            inventoryService.deleteProduct(productId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/products/{productId}/stock")
    public ResponseEntity<?> updateProductStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            Product product = inventoryService.updateProductStock(productId, quantity);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Stock updated successfully");
            response.put("product", product);
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
    
    // ========== TELEGRAM ==========
    
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