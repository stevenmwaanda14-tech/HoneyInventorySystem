/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.service;

/**
 *
 * @author hp
 */



import com.teash.inventory.entity.*;
import com.teash.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InventoryService {
    
    @Autowired
    private RawMaterialRepository rawMaterialRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FinishedGoodsRepository finishedGoodsRepository;
    
    @Autowired
    private ProductionRunRepository productionRunRepository;
    
    @Autowired
    private SaleRepository saleRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // ========== GET ALL DATA ==========
    
    public List<RawMaterial> getAllMaterials() {
        return rawMaterialRepository.findAll();
    }
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public List<FinishedGoods> getAllFinishedGoods() {
        return finishedGoodsRepository.findAll();
    }
    
    public List<ProductionRun> getProductionHistory() {
        return productionRunRepository.findTop50ByOrderByProductionDateDesc();
    }
    
    public List<Sale> getSalesHistory() {
        return saleRepository.findTop50ByOrderBySaleDateDesc();
    }
    
    public List<Notification> getRecentNotifications() {
        return notificationRepository.findTop50ByOrderBySentAtDesc();
    }
    
    // ========== RAW MATERIALS ==========
    
    @Transactional
    public RawMaterial receiveMaterials(Long materialId, Integer quantity, BigDecimal costPerUnit, String supplier) {
        RawMaterial material = rawMaterialRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Material not found with ID: " + materialId));
        
        material.addQuantity(quantity);
        rawMaterialRepository.save(material);
        
        String message = String.format(
            "📦 Received %d %s\nNew Total: %d units",
            quantity, material.getName(), material.getQuantity()
        );
        
        System.out.println("✅ " + message);
        
        notificationService.sendTelegramCustomAlert("📦 Material Received", message);
        
        if (!material.isBelowThreshold()) {
            notificationService.sendTelegramCustomAlert(
                "✅ Stock Restored",
                material.getName() + " is now at " + material.getQuantity() + " units"
            );
        }
        
        return material;
    }
    
    @Transactional
    public RawMaterial updateThreshold(Long materialId, Integer newThreshold) {
        RawMaterial material = rawMaterialRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Material not found with ID: " + materialId));
        
        Integer oldThreshold = material.getMinThreshold();
        material.setMinThreshold(newThreshold);
        RawMaterial updated = rawMaterialRepository.save(material);
        
        String message = String.format(
            "⚡ Threshold Updated\nMaterial: %s\nOld: %d\nNew: %d",
            material.getName(), oldThreshold, newThreshold
        );
        
        System.out.println("✅ " + message);
        notificationService.sendTelegramCustomAlert("⚡ Threshold Updated", message);
        
        return updated;
    }
    
    @Transactional
    public RawMaterial updateMaterialQuantity(String materialName, Integer newQuantity) {
        RawMaterial material = rawMaterialRepository.findByName(materialName)
            .orElseThrow(() -> new RuntimeException("Material not found: " + materialName));
        
        Integer oldQuantity = material.getQuantity();
        material.setQuantity(newQuantity);
        material.setLastUpdated(LocalDateTime.now());
        RawMaterial updated = rawMaterialRepository.save(material);
        
        String message = String.format(
            "📊 Stock Manually Updated\nMaterial: %s\nOld: %d\nNew: %d",
            materialName, oldQuantity, newQuantity
        );
        
        System.out.println("✅ " + message);
        notificationService.sendTelegramCustomAlert("📊 Stock Updated", message);
        
        if (material.isBelowThreshold()) {
            notificationService.sendTelegramStockAlert(
                material.getName(),
                material.getQuantity(),
                material.getMinThreshold(),
                material.isOutOfStock()
            );
        }
        
        return updated;
    }
    
    // ========== PRODUCTION ==========
    
    @Transactional
    public FinishedGoods buildPacks(Long productId, Integer packsToBuild, String producedBy) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        
        Integer jarsNeeded = packsToBuild * product.getJarsPerPack();
        Integer stickersNeeded = packsToBuild;
        Integer boxesNeeded = packsToBuild;
        
        RawMaterial jars = rawMaterialRepository.findByName("Jars")
            .orElseThrow(() -> new RuntimeException("Jars material not found"));
        RawMaterial stickers = rawMaterialRepository.findByName("Stickers")
            .orElseThrow(() -> new RuntimeException("Stickers material not found"));
        RawMaterial boxes = rawMaterialRepository.findByName("Boxes")
            .orElseThrow(() -> new RuntimeException("Boxes material not found"));
        
        validateMaterials(jars, jarsNeeded, "Jars");
        validateMaterials(stickers, stickersNeeded, "Stickers");
        validateMaterials(boxes, boxesNeeded, "Boxes");
        
        jars.subtractQuantity(jarsNeeded);
        stickers.subtractQuantity(stickersNeeded);
        boxes.subtractQuantity(boxesNeeded);
        
        rawMaterialRepository.saveAll(Arrays.asList(jars, stickers, boxes));
        
        FinishedGoods finished = finishedGoodsRepository.findByProduct(product)
            .orElse(new FinishedGoods(product, 0));
        finished.addPacks(packsToBuild);
        finishedGoodsRepository.save(finished);
        
        ProductionRun run = new ProductionRun();
        run.setProduct(product);
        run.setPacksProduced(packsToBuild);
        run.setJarsUsed(jarsNeeded);
        run.setStickersUsed(stickersNeeded);
        run.setBoxesUsed(boxesNeeded);
        run.setProducedBy(producedBy != null ? producedBy : "System");
        productionRunRepository.save(run);
        
        System.out.println("✅ Built " + packsToBuild + " packs of " + product.getName());
        
        checkMaterialsAfterOperation(Arrays.asList(jars, stickers, boxes));
        
        return finished;
    }
    
    private void validateMaterials(RawMaterial material, Integer needed, String name) {
        if (material.getQuantity() < needed) {
            throw new IllegalArgumentException(
                String.format("Not enough %s. Need %d, have %d", name, needed, material.getQuantity())
            );
        }
    }
    
    private void checkMaterialsAfterOperation(List<RawMaterial> materials) {
        for (RawMaterial material : materials) {
            if (material.isBelowThreshold()) {
                notificationService.sendTelegramStockAlert(
                    material.getName(),
                    material.getQuantity(),
                    material.getMinThreshold(),
                    material.isOutOfStock()
                );
            }
        }
    }
    
    // ========== SALES ==========
    
    @Transactional
    public FinishedGoods sellPacks(Long productId, Integer packsToSell, String customerName, BigDecimal salePrice) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        
        FinishedGoods finished = finishedGoodsRepository.findByProduct(product)
            .orElseThrow(() -> new RuntimeException("Product not found in inventory"));
        
        finished.subtractPacks(packsToSell);
        finishedGoodsRepository.save(finished);
        
        Sale sale = new Sale();
        sale.setProduct(product);
        sale.setQuantityPacks(packsToSell);
        sale.setCustomerName(customerName);
        sale.setSalePrice(salePrice);
        saleRepository.save(sale);
        
        System.out.println("💰 Sold " + packsToSell + " of " + product.getName());
        
        notificationService.sendTelegramSalesAlert(product.getName(), packsToSell, finished.getQuantityPacks());
        
        if (finished.getQuantityPacks() < 20) {
            notificationService.sendTelegramCustomAlert(
                "⚠️ Low Stock Warning",
                product.getName() + " has only " + finished.getQuantityPacks() + " packs remaining!"
            );
        }
        
        return finished;
    }
    
    // ========== PRODUCT MANAGEMENT ==========
    
    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku).orElse(null);
    }
    
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    
    @Transactional
    public Product addNewProduct(String name, String sku, Integer initialStock, Integer jarsPerPack) {
        // Check if SKU already exists
        if (productRepository.findBySku(sku).isPresent()) {
            throw new RuntimeException("SKU already exists: " + sku);
        }
        
        // Create product
        Product product = new Product(name, sku);
        if (jarsPerPack != null && jarsPerPack > 0) {
            product.setJarsPerPack(jarsPerPack);
        }
        Product saved = productRepository.save(product);
        
        // Add initial stock if provided
        if (initialStock != null && initialStock > 0) {
            addInitialStock(saved.getId(), initialStock);
        }
        
        return saved;
    }
    
    @Transactional
    public void addInitialStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        FinishedGoods finished = finishedGoodsRepository.findByProduct(product)
            .orElse(new FinishedGoods(product, 0));
        finished.addPacks(quantity);
        finishedGoodsRepository.save(finished);
    }
    
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Delete finished goods first
        finishedGoodsRepository.findByProduct(product).ifPresent(finishedGoodsRepository::delete);
        
        // Delete the product
        productRepository.delete(product);
        
        System.out.println("🗑️ Product deleted: " + product.getName());
    }
    
    @Transactional
    public Product updateProductStock(Long productId, Integer newQuantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        FinishedGoods finished = finishedGoodsRepository.findByProduct(product)
            .orElse(new FinishedGoods(product, 0));
        
        // Just update the quantity directly
        finished.setQuantityPacks(newQuantity);
        finished.setLastUpdated(LocalDateTime.now());
        finishedGoodsRepository.save(finished);
        
        return product;
    }
    
    // ========== DASHBOARD ==========
    
    public Map<String, Object> getDashboardStatus() {
        Map<String, Object> status = new HashMap<>();
        
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        status.put("rawMaterials", materials);
        
        List<FinishedGoods> finished = finishedGoodsRepository.findAll();
        status.put("finishedGoods", finished);
        
        List<Map<String, Object>> alerts = checkStockAndGetAlerts();
        status.put("alerts", alerts);
        
        status.put("totalProducts", productRepository.count());
        status.put("lastUpdated", LocalDateTime.now());
        
        return status;
    }
    
    public List<Map<String, Object>> checkStockAndGetAlerts() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        for (RawMaterial material : materials) {
            if (material.isBelowThreshold()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("materialId", material.getId());
                alert.put("material", material.getName());
                alert.put("quantity", material.getQuantity());
                alert.put("threshold", material.getMinThreshold());
                alert.put("status", material.isOutOfStock() ? "CRITICAL" : "LOW");
                alerts.add(alert);
            }
        }
        
        return alerts;
    }
}