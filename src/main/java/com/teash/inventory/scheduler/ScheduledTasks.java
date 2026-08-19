/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author hp
 */

package com.teash.inventory.scheduler;

import com.teash.inventory.entity.RawMaterial;
import com.teash.inventory.entity.FinishedGoods;
import com.teash.inventory.entity.UserSettings;
import com.teash.inventory.repository.RawMaterialRepository;
import com.teash.inventory.repository.FinishedGoodsRepository;
import com.teash.inventory.repository.UserSettingsRepository;
import com.teash.inventory.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ScheduledTasks {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private RawMaterialRepository rawMaterialRepository;
    
    @Autowired
    private FinishedGoodsRepository finishedGoodsRepository;
    
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // =============================================
    // STOCK CHECK - Every 30 minutes
    // =============================================
    
    @Scheduled(fixedRate = 1800000)
    public void checkStockAndSendAlerts() {
        System.out.println("🔄 Running scheduled stock check at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            List<UserSettings> activeUsers = userSettingsRepository.findByIsActiveTrue();
            if (activeUsers.isEmpty()) {
                System.out.println("⚠️ No active users found for notifications.");
                return;
            }
            
            notificationService.checkAndSendAlerts();
            System.out.println("✅ Stock check completed successfully.");
            
        } catch (Exception e) {
            System.err.println("❌ Error during stock check: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =============================================
    // DAILY SUMMARY - Every day at 9:00 AM
    // =============================================
    
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyStockSummary() {
        System.out.println("📊 Running daily stock summary at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            List<UserSettings> usersForSummary = userSettingsRepository.findByNotifyDailySummaryTrueAndIsActiveTrue();
            if (usersForSummary.isEmpty()) {
                System.out.println("ℹ️ No users opted in for daily summary.");
                return;
            }
            
            List<RawMaterial> materials = rawMaterialRepository.findAll();
            List<FinishedGoods> finished = finishedGoodsRepository.findAll();
            
            // Build summary message
            StringBuilder message = new StringBuilder();
            message.append("📊 <b>DAILY INVENTORY SUMMARY</b>\n");
            message.append("─────────────────────────\n");
            message.append("📅 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
            
            message.append("📦 <b>Raw Materials:</b>\n");
            for (RawMaterial material : materials) {
                String status = material.isOutOfStock() ? "🔴 OUT OF STOCK" :
                               material.isBelowThreshold() ? "🟡 LOW" : "🟢 OK";
                message.append("  • ").append(material.getName()).append(": ").append(material.getQuantity()).append(" units (").append(status).append(")\n");
            }
            
            message.append("\n📦 <b>Finished Goods:</b>\n");
            for (FinishedGoods good : finished) {
                String productName = good.getProduct() != null ? good.getProduct().getName() : "Unknown";
                String status = good.getQuantityPacks() == 0 ? "🔴 OUT OF STOCK" : "✅ Available";
                message.append("  • ").append(productName).append(": ").append(good.getQuantityPacks()).append(" packs (").append(status).append(")\n");
            }
            
            // Send to all users who want daily summary
            for (UserSettings user : usersForSummary) {
                notificationService.sendTelegramMessageToChat(message.toString(), user.getTelegramChatId());
                System.out.println("📧 Daily summary sent to: " + user.getUserEmail());
            }
            
            System.out.println("✅ Daily summary completed successfully.");
            
        } catch (Exception e) {
            System.err.println("❌ Error during daily summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =============================================
    // EVENING STOCK CHECK - Every day at 6:00 PM
    // =============================================
    
    @Scheduled(cron = "0 0 18 * * *")
    public void eveningStockCheck() {
        System.out.println("🌙 Running evening stock check at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            notificationService.checkAndSendAlerts();
            
            List<RawMaterial> materials = rawMaterialRepository.findAll();
            List<FinishedGoods> finished = finishedGoodsRepository.findAll();
            List<UserSettings> eveningUsers = userSettingsRepository.findByIsActiveTrue();
            
            if (!eveningUsers.isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append("🌙 <b>EVENING INVENTORY STATUS</b>\n");
                message.append("─────────────────────────\n");
                message.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
                
                message.append("📦 <b>Raw Materials:</b>\n");
                for (RawMaterial material : materials) {
                    String status = material.isOutOfStock() ? "🔴 OUT OF STOCK" :
                                   material.isBelowThreshold() ? "🟡 LOW" : "🟢 OK";
                    message.append("  • ").append(material.getName()).append(": ").append(material.getQuantity()).append(" units (").append(status).append(")\n");
                }
                
                message.append("\n📦 <b>Finished Goods:</b>\n");
                for (FinishedGoods good : finished) {
                    String productName = good.getProduct() != null ? good.getProduct().getName() : "Unknown";
                    message.append("  • ").append(productName).append(": ").append(good.getQuantityPacks()).append(" packs\n");
                }
                
                UserSettings firstUser = eveningUsers.get(0);
                notificationService.sendTelegramMessageToChat(message.toString(), firstUser.getTelegramChatId());
                System.out.println("🌙 Evening summary sent to: " + firstUser.getUserEmail());
            }
            
            System.out.println("✅ Evening stock check completed.");
            
        } catch (Exception e) {
            System.err.println("❌ Error during evening stock check: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =============================================
    // WEEKLY SUMMARY - Every Monday at 8:00 AM
    // =============================================
    
    @Scheduled(cron = "0 0 8 * * MON")
    public void weeklyStockSummary() {
        System.out.println("📊 Running weekly stock summary at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            List<RawMaterial> materials = rawMaterialRepository.findAll();
            List<FinishedGoods> finished = finishedGoodsRepository.findAll();
            List<UserSettings> activeUsers = userSettingsRepository.findByIsActiveTrue();
            
            if (activeUsers.isEmpty()) {
                System.out.println("ℹ️ No active users found.");
                return;
            }
            
            StringBuilder message = new StringBuilder();
            message.append("📊 <b>WEEKLY INVENTORY REPORT</b>\n");
            message.append("─────────────────────────\n");
            message.append("📅 Week of: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))).append("\n\n");
            
            message.append("📦 <b>RAW MATERIALS</b>\n");
            for (RawMaterial material : materials) {
                String status = material.isOutOfStock() ? "🔴 OUT OF STOCK" :
                               material.isBelowThreshold() ? "🟡 LOW" : "🟢 OK";
                message.append("  • ").append(material.getName()).append(": ").append(material.getQuantity()).append(" units (").append(status).append(")\n");
            }
            
            message.append("\n📦 <b>FINISHED GOODS</b>\n");
            for (FinishedGoods good : finished) {
                String productName = good.getProduct() != null ? good.getProduct().getName() : "Unknown";
                message.append("  • ").append(productName).append(": ").append(good.getQuantityPacks()).append(" packs\n");
            }
            
            message.append("\n─────────────────────────\n");
            boolean hasCritical = false;
            for (RawMaterial material : materials) {
                if (material.isOutOfStock()) {
                    hasCritical = true;
                    message.append("🔴 CRITICAL: ").append(material.getName()).append(" is OUT OF STOCK!\n");
                }
            }
            if (!hasCritical) {
                message.append("✅ All materials are in stock!\n");
            }
            
            message.append("\n📅 Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            UserSettings firstUser = activeUsers.get(0);
            notificationService.sendTelegramMessageToChat(message.toString(), firstUser.getTelegramChatId());
            System.out.println("✅ Weekly summary sent to: " + firstUser.getUserEmail());
            
        } catch (Exception e) {
            System.err.println("❌ Error during weekly summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
}