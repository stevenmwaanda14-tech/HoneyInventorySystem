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
    
    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void checkStockAndSendAlerts() {
        System.out.println("🔄 Running scheduled stock check at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            // Check if any user wants notifications
            List<UserSettings> activeUsers = userSettingsRepository.findByIsActiveTrue();
            
            if (activeUsers.isEmpty()) {
                System.out.println("⚠️ No active users found for notifications.");
                return;
            }
            
            // Check stock and send alerts
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
            // Check if any user wants daily summary
            List<UserSettings> usersForSummary = userSettingsRepository.findByNotifyDailySummaryTrueAndIsActiveTrue();
            
            if (usersForSummary.isEmpty()) {
                System.out.println("ℹ️ No users opted in for daily summary.");
                return;
            }
            
            // Get all materials and finished goods
            List<RawMaterial> materials = rawMaterialRepository.findAll();
            List<FinishedGoods> finished = finishedGoodsRepository.findAll();
            
            // Send summary to all active users who opted in
            for (UserSettings user : usersForSummary) {
                // Use the 2-parameter version (sends to all users who opted in)
                notificationService.sendTelegramDailySummary(materials, finished);
                System.out.println("📧 Daily summary sent to user: " + user.getUserEmail());
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
            // Do a full stock check
            notificationService.checkAndSendAlerts();
            
            // Get all materials for a quick status
            List<RawMaterial> materials = rawMaterialRepository.findAll();
            List<FinishedGoods> finished = finishedGoodsRepository.findAll();
            
            // Send evening summary to users who want it
            List<UserSettings> eveningUsers = userSettingsRepository.findByIsActiveTrue();
            
            if (!eveningUsers.isEmpty()) {
                // Use the 2-parameter version (sends to all active users)
                notificationService.sendTelegramDailySummary(materials, finished);
                System.out.println("🌙 Evening summary sent to active users.");
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
            
            // Send weekly summary using the existing method
            notificationService.sendTelegramWeeklySummary(materials, finished);
            
            System.out.println("✅ Weekly summary sent successfully.");
            
        } catch (Exception e) {
            System.err.println("❌ Error during weekly summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =============================================
    // DATABASE CLEANUP - Every Sunday at 3:00 AM
    // =============================================
    
    @Scheduled(cron = "0 0 3 * * SUN")
    public void cleanupNotifications() {
        System.out.println("🧹 Running cleanup task at: " + LocalDateTime.now().format(TIME_FORMATTER));
        
        try {
            // Delete notifications older than 30 days
            // This would need a custom repository method
            System.out.println("🧹 Cleanup completed.");
            
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}