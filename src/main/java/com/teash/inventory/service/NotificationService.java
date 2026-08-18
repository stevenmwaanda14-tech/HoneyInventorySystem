/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.service;

/**
 *
 * @author hp
 */



import com.teash.inventory.entity.Notification;
import com.teash.inventory.entity.RawMaterial;
import com.teash.inventory.entity.FinishedGoods;
import com.teash.inventory.entity.UserSettings;
import com.teash.inventory.repository.NotificationRepository;
import com.teash.inventory.repository.RawMaterialRepository;
import com.teash.inventory.repository.FinishedGoodsRepository;
import com.teash.inventory.repository.UserSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    // =============================================
    // TELEGRAM CONFIGURATION - UPDATE THESE!
    // =============================================
    
    // REPLACE with your bot token from @BotFather
    private static final String TELEGRAM_BOT_TOKEN = "1234567890:ABCdefGHIjklMNOpqrsTUVwxyz";
    
    // DEFAULT CHAT ID (used if no user settings found)
    private static final String DEFAULT_CHAT_ID = "987654321";
    
    // =============================================
    // NTFY CONFIGURATION (Backup - Optional)
    // =============================================
    
    @Value("${ntfy.topic:honey-inventory-alerts}")
    private String ntfyTopic;
    
    @Value("${ntfy.url:https://ntfy.sh}")
    private String ntfyUrl;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private RawMaterialRepository rawMaterialRepository;
    
    @Autowired
    private FinishedGoodsRepository finishedGoodsRepository;
    
    @Autowired
    private UserSettingsRepository userSettingsRepository;
    
    private final WebClient webClient;
    
    public NotificationService() {
        this.webClient = WebClient.builder().build();
    }
    
    // =============================================
    // CORE TELEGRAM SEND METHODS
    // =============================================
    
    public void sendTelegramMessage(String message) {
        List<UserSettings> activeUsers = userSettingsRepository.findByIsActiveTrue();
        
        if (activeUsers.isEmpty()) {
            sendTelegramMessageToChat(message, DEFAULT_CHAT_ID);
            System.out.println("⚠️ No active users found. Sent to default chat ID.");
            return;
        }
        
        for (UserSettings user : activeUsers) {
            if (user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty()) {
                sendTelegramMessageToChat(message, user.getTelegramChatId());
            }
        }
    }
    
    public void sendTelegramMessageToChat(String message, String chatId) {
        if (chatId == null || chatId.isEmpty()) {
            System.err.println("❌ Cannot send Telegram message: Chat ID is null or empty");
            return;
        }
        
        try {
            String urlString = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
            
            String json = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\",\"parse_mode\":\"HTML\"}",
                chatId, escapedMessage
            );
            
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println("✅ Telegram message sent to chat: " + chatId);
            } else {
                System.err.println("❌ Telegram failed for chat " + chatId + ". Response: " + responseCode);
            }
            conn.disconnect();
            
        } catch (Exception e) {
            System.err.println("❌ Telegram error for chat " + chatId + ": " + e.getMessage());
        }
    }
    
    // =============================================
    // STOCK ALERT METHODS
    // =============================================
    
    public void sendTelegramStockAlert(String material, int quantity, int threshold, boolean isCritical) {
        String emoji = isCritical ? "🚨" : "⚠️";
        String status = isCritical ? "CRITICAL - OUT OF STOCK!" : "LOW STOCK";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String message = String.format(
            "%s <b>%s</b>\n" +
            "─────────────────\n" +
            "📦 Material: %s\n" +
            "📊 Quantity: <b>%d</b>\n" +
            "⚡ Threshold: %d\n" +
            "📅 Date: %s\n" +
            "🕐 Time: %s\n" +
            "─────────────────\n" +
            "%s",
            emoji, status, material, quantity, threshold, date, time,
            isCritical ? "🔴 <b>ACTION REQUIRED!</b> Please restock immediately!" : "🟡 Please check inventory soon."
        );
        
        sendTelegramMessage(message);
    }
    
    public void sendTelegramStockAlertToUser(String material, int quantity, int threshold, boolean isCritical, String chatId) {
        String emoji = isCritical ? "🚨" : "⚠️";
        String status = isCritical ? "CRITICAL - OUT OF STOCK!" : "LOW STOCK";
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        String message = String.format(
            "%s <b>%s</b>\n" +
            "─────────────────\n" +
            "📦 Material: %s\n" +
            "📊 Quantity: <b>%d</b>\n" +
            "⚡ Threshold: %d\n" +
            "🕐 Time: %s\n" +
            "─────────────────\n" +
            "%s",
            emoji, status, material, quantity, threshold, time,
            isCritical ? "🔴 <b>ACTION REQUIRED!</b> Please restock immediately!" : "🟡 Please check inventory soon."
        );
        
        sendTelegramMessageToChat(message, chatId);
    }
    
    // =============================================
    // SALES ALERT METHODS (Updated)
    // =============================================
    
    public void sendTelegramSalesAlert(String product, int packsSold, int remaining) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        String message = String.format(
            "💰 <b>Sale Recorded</b>\n" +
            "─────────────────\n" +
            "📦 Product: %s\n" +
            "📊 Products Sold: <b>%d</b>\n" +
            "📦 Remaining Stock: <b>%d</b>\n" +
            "📅 Date: %s\n" +
            "🕐 Time: %s",
            product, packsSold, remaining, date, time
        );
        
        List<UserSettings> users = userSettingsRepository.findByIsActiveTrue();
        for (UserSettings user : users) {
            if (user.getNotifySales() != null && user.getNotifySales()) {
                sendTelegramMessageToChat(message, user.getTelegramChatId());
            }
        }
    }
    
    public void sendTelegramSalesAlertToUser(String product, int packsSold, int remaining, String chatId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        String message = String.format(
            "💰 <b>Sale Recorded</b>\n" +
            "─────────────────\n" +
            "📦 Product: %s\n" +
            "📊 Products Sold: <b>%d</b>\n" +
            "📦 Remaining Stock: <b>%d</b>\n" +
            "🕐 Time: %s",
            product, packsSold, remaining, time
        );
        
        sendTelegramMessageToChat(message, chatId);
    }
    
    // =============================================
    // SUMMARY METHODS
    // =============================================
    
    public void sendTelegramDailySummary(List<RawMaterial> materials, List<FinishedGoods> finished) {
        List<UserSettings> users = userSettingsRepository.findByNotifyDailySummaryTrueAndIsActiveTrue();
        
        if (users.isEmpty()) {
            System.out.println("ℹ️ No users opted in for daily summary.");
            return;
        }
        
        String summary = buildSummaryMessage(materials, finished, "DAILY");
        
        for (UserSettings user : users) {
            sendTelegramMessageToChat(summary, user.getTelegramChatId());
            System.out.println("📧 Daily summary sent to: " + user.getUserEmail());
        }
    }
    
    public void sendTelegramDailySummary(List<RawMaterial> materials, List<FinishedGoods> finished, String chatId) {
        String summary = buildSummaryMessage(materials, finished, "DAILY");
        sendTelegramMessageToChat(summary, chatId);
    }
    
    public void sendTelegramWeeklySummary(List<RawMaterial> materials, List<FinishedGoods> finished) {
        List<UserSettings> users = userSettingsRepository.findByIsActiveTrue();
        
        if (users.isEmpty()) {
            System.out.println("ℹ️ No active users found.");
            return;
        }
        
        String summary = buildSummaryMessage(materials, finished, "WEEKLY");
        
        for (UserSettings user : users) {
            sendTelegramMessageToChat(summary, user.getTelegramChatId());
            System.out.println("📧 Weekly summary sent to: " + user.getUserEmail());
        }
    }
    
    private String buildSummaryMessage(List<RawMaterial> materials, List<FinishedGoods> finished, String type) {
        StringBuilder message = new StringBuilder();
        message.append("📊 <b>").append(type).append(" INVENTORY SUMMARY</b>\n");
        message.append("─────────────────────────\n");
        message.append("📅 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
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
        boolean hasLow = false;
        
        for (RawMaterial material : materials) {
            if (material.isOutOfStock()) {
                hasCritical = true;
                message.append("🔴 CRITICAL: ").append(material.getName()).append(" is OUT OF STOCK!\n");
            } else if (material.isBelowThreshold()) {
                hasLow = true;
                message.append("🟡 WARNING: ").append(material.getName()).append(" is LOW (").append(material.getQuantity()).append(" units)\n");
            }
        }
        
        if (!hasCritical && !hasLow) {
            message.append("✅ All materials are in stock!\n");
        }
        
        return message.toString();
    }
    
    // =============================================
    // TELEGRAM BOT COMMAND HANDLERS
    // =============================================
    
    public void handleTelegramCommand(String chatId, String command) {
        String response = "";
        
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        command = command.trim().toLowerCase();
        
        switch (command) {
            case "start":
                response = getWelcomeMessage();
                break;
            case "stock":
                response = getStockSummary();
                break;
            case "materials":
                response = getMaterialsSummary();
                break;
            case "finished":
                response = getFinishedGoodsSummary();
                break;
            case "alerts":
                response = getAlertSummary();
                break;
            case "help":
                response = getHelpMessage();
                break;
            case "status":
                response = getQuickStatus();
                break;
            default:
                response = "❌ Unknown command. Type /help for available commands.";
        }
        
        sendTelegramMessageToChat(response, chatId);
    }
    
    private String getWelcomeMessage() {
        return "🍯 <b>Welcome to Honey Inventory Bot!</b>\n\n" +
               "I'll help you manage your honey inventory.\n\n" +
               "📋 <b>Commands:</b>\n" +
               "/stock - Full inventory status\n" +
               "/materials - Raw materials only\n" +
               "/finished - Finished goods only\n" +
               "/alerts - View active alerts\n" +
               "/status - Quick status summary\n" +
               "/help - Show this message\n\n" +
               "🔔 You'll receive automatic alerts when stock is low!";
    }
    
    private String getHelpMessage() {
        return "🍯 <b>Honey Inventory Bot - Help</b>\n\n" +
               "📋 <b>Commands:</b>\n" +
               "/stock - Full inventory status\n" +
               "/materials - Raw materials only\n" +
               "/finished - Finished goods only\n" +
               "/alerts - View active alerts\n" +
               "/status - Quick status summary\n" +
               "/help - Show this message\n\n" +
               "🔔 <b>Automatic Alerts:</b>\n" +
               "You'll receive notifications when:\n" +
               "• Any material goes below threshold\n" +
               "• Any material is OUT OF STOCK\n" +
               "• Sales are recorded\n\n" +
               "📊 <b>Daily Summary:</b>\n" +
               "You'll get a daily summary at 9:00 AM";
    }
    
    private String getStockSummary() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        List<FinishedGoods> finished = finishedGoodsRepository.findAll();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 <b>FULL INVENTORY SUMMARY</b>\n");
        sb.append("─────────────────────────\n");
        sb.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        
        sb.append("📦 <b>Raw Materials:</b>\n");
        for (RawMaterial m : materials) {
            String status = m.isOutOfStock() ? "🔴 OUT" : 
                           m.isBelowThreshold() ? "🟡 LOW" : "🟢 OK";
            sb.append("  • ").append(m.getName()).append(": <b>").append(m.getQuantity()).append("</b> (").append(status).append(")\n");
        }
        
        sb.append("\n📦 <b>Finished Goods:</b>\n");
        for (FinishedGoods f : finished) {
            String name = f.getProduct() != null ? f.getProduct().getName() : "Unknown";
            String status = f.getQuantityPacks() == 0 ? "🔴 OUT OF STOCK" : 
                           f.getQuantityPacks() < 10 ? "🟡 LOW" : "🟢 OK";
            sb.append("  • ").append(name).append(": <b>").append(f.getQuantityPacks()).append("</b> packs (").append(status).append(")\n");
        }
        
        return sb.toString();
    }
    
    private String getMaterialsSummary() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📦 <b>RAW MATERIALS</b>\n");
        sb.append("─────────────────\n");
        sb.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        
        for (RawMaterial m : materials) {
            String status = m.isOutOfStock() ? "🔴 OUT OF STOCK" : 
                           m.isBelowThreshold() ? "🟡 LOW" : "🟢 OK";
            sb.append("<b>").append(m.getName()).append("</b>\n");
            sb.append("  Quantity: <b>").append(m.getQuantity()).append("</b>\n");
            sb.append("  Threshold: ").append(m.getMinThreshold()).append("\n");
            sb.append("  Status: ").append(status).append("\n\n");
        }
        
        return sb.toString();
    }
    
    private String getFinishedGoodsSummary() {
        List<FinishedGoods> finished = finishedGoodsRepository.findAll();
        
        StringBuilder sb = new StringBuilder();
        sb.append("📦 <b>FINISHED GOODS</b>\n");
        sb.append("─────────────────\n");
        sb.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        
        int totalPacks = 0;
        for (FinishedGoods f : finished) {
            String name = f.getProduct() != null ? f.getProduct().getName() : "Unknown";
            String status = f.getQuantityPacks() == 0 ? "🔴 OUT OF STOCK" : 
                           f.getQuantityPacks() < 10 ? "🟡 LOW" : "🟢 OK";
            sb.append("<b>").append(name).append("</b>\n");
            sb.append("  Packs: <b>").append(f.getQuantityPacks()).append("</b>\n");
            sb.append("  Status: ").append(status).append("\n\n");
            totalPacks += f.getQuantityPacks();
        }
        
        sb.append("─────────────────\n");
        sb.append("📊 <b>Total Products: ").append(totalPacks).append("</b>");
        
        return sb.toString();
    }
    
    private String getAlertSummary() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        for (RawMaterial m : materials) {
            if (m.isBelowThreshold()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("name", m.getName());
                alert.put("quantity", m.getQuantity());
                alert.put("threshold", m.getMinThreshold());
                alert.put("status", m.isOutOfStock() ? "CRITICAL" : "LOW");
                alerts.add(alert);
            }
        }
        
        if (alerts.isEmpty()) {
            return "✅ <b>No active alerts!</b>\n\nAll materials are above their thresholds.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("🚨 <b>ACTIVE ALERTS</b>\n");
        sb.append("─────────────────\n");
        sb.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        
        for (Map<String, Object> alert : alerts) {
            String emoji = alert.get("status").equals("CRITICAL") ? "🔴" : "🟡";
            sb.append(emoji).append(" <b>").append(alert.get("name")).append("</b>\n");
            sb.append("  Quantity: <b>").append(alert.get("quantity")).append("</b>\n");
            sb.append("  Threshold: ").append(alert.get("threshold")).append("\n");
            sb.append("  Status: <b>").append(alert.get("status")).append("</b>\n\n");
        }
        
        return sb.toString();
    }
    
    private String getQuickStatus() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        List<FinishedGoods> finished = finishedGoodsRepository.findAll();
        
        int totalPacks = 0;
        int lowStock = 0;
        int critical = 0;
        
        for (FinishedGoods f : finished) {
            totalPacks += f.getQuantityPacks();
        }
        
        for (RawMaterial m : materials) {
            if (m.isOutOfStock()) critical++;
            else if (m.isBelowThreshold()) lowStock++;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📊 <b>QUICK STATUS</b>\n");
        sb.append("─────────────────\n");
        sb.append("🕐 ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n\n");
        sb.append("📦 Total Products: <b>").append(totalPacks).append("</b>\n");
        sb.append("📦 Materials: <b>").append(materials.size()).append("</b>\n");
        sb.append("🟡 Low Stock: <b>").append(lowStock).append("</b>\n");
        sb.append("🔴 Critical: <b>").append(critical).append("</b>\n");
        
        if (critical > 0) {
            sb.append("\n🚨 <b>CRITICAL ALERTS!</b> Check /alerts");
        } else if (lowStock > 0) {
            sb.append("\n⚠️ <b>Low stock alerts!</b> Check /alerts");
        } else {
            sb.append("\n✅ <b>All stock levels are good!</b>");
        }
        
        return sb.toString();
    }
    
    // =============================================
    // CUSTOM ALERT METHODS
    // =============================================
    
    public void sendTelegramCustomAlert(String title, String message) {
        String formattedMessage = String.format(
            "📢 <b>%s</b>\n─────────────────\n%s",
            title, message
        );
        sendTelegramMessage(formattedMessage);
    }
    
    public void sendTelegramCustomAlertToUser(String title, String message, String chatId) {
        String formattedMessage = String.format(
            "📢 <b>%s</b>\n─────────────────\n%s",
            title, message
        );
        sendTelegramMessageToChat(formattedMessage, chatId);
    }
    
    // =============================================
    // TEST METHODS
    // =============================================
    
    public void testTelegramConnection() {
        String testMessage = "✅ <b>Connection Test Successful!</b>\n\n" +
            "Your Honey Inventory System is connected to Telegram!\n" +
            "📅 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n" +
            "Try these commands:\n" +
            "/stock - View full inventory\n" +
            "/materials - View raw materials\n" +
            "/finished - View finished goods\n" +
            "/alerts - View active alerts\n" +
            "/status - Quick status summary";
        
        List<UserSettings> users = userSettingsRepository.findByIsActiveTrue();
        if (users.isEmpty()) {
            sendTelegramMessageToChat(testMessage, DEFAULT_CHAT_ID);
            System.out.println("📱 Test message sent to default chat ID!");
        } else {
            for (UserSettings user : users) {
                sendTelegramMessageToChat(testMessage, user.getTelegramChatId());
                System.out.println("📱 Test message sent to: " + user.getUserEmail());
            }
        }
    }
    
    // =============================================
    // NTFY PUSH NOTIFICATION (Backup)
    // =============================================
    
    public Mono<Map<String, Object>> sendPushNotification(String title, String message, String priority, String tags) {
        String url = ntfyUrl + "/" + ntfyTopic;
        
        Map<String, Object> response = new HashMap<>();
        
        return webClient.post()
            .uri(url)
            .header("Title", title)
            .header("Priority", priority != null ? priority : "default")
            .header("Tags", tags != null ? tags : "information")
            .bodyValue(message)
            .retrieve()
            .toBodilessEntity()
            .map(v -> {
                System.out.println("✅ Push notification sent: " + title);
                response.put("success", true);
                response.put("message", "Push notification sent successfully");
                return response;
            })
            .onErrorResume(e -> {
                System.err.println("❌ Push notification failed: " + e.getMessage());
                response.put("success", false);
                response.put("message", "Push failed: " + e.getMessage());
                return Mono.just(response);
            });
    }
    
    // =============================================
    // MAIN STOCK CHECK & ALERT METHOD
    // =============================================
    
    public void checkAndSendAlerts() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        int alertsSent = 0;
        StringBuilder alertSummary = new StringBuilder();
        
        List<UserSettings> lowStockUsers = userSettingsRepository.findByNotifyLowStockTrueAndIsActiveTrue();
        List<UserSettings> criticalUsers = userSettingsRepository.findByNotifyCriticalStockTrueAndIsActiveTrue();
        
        for (RawMaterial material : materials) {
            if (material.isBelowThreshold()) {
                List<Notification> recent = notificationRepository
                    .findByMaterialAndSentAtAfter(material, oneHourAgo);
                
                if (recent.isEmpty()) {
                    boolean isCritical = material.isOutOfStock();
                    
                    List<UserSettings> targetUsers = isCritical ? criticalUsers : lowStockUsers;
                    
                    if (targetUsers.isEmpty()) {
                        targetUsers = userSettingsRepository.findByIsActiveTrue();
                    }
                    
                    for (UserSettings user : targetUsers) {
                        if (user.getTelegramChatId() != null && !user.getTelegramChatId().isEmpty()) {
                            sendTelegramStockAlertToUser(
                                material.getName(),
                                material.getQuantity(),
                                material.getMinThreshold(),
                                isCritical,
                                user.getTelegramChatId()
                            );
                        }
                    }
                    
                    Notification notification = new Notification();
                    notification.setMaterial(material);
                    notification.setAlertType(isCritical ? "CRITICAL" : "LOW_STOCK");
                    notification.setMessage(String.format(
                        "%s is at %d units (Threshold: %d)",
                        material.getName(), material.getQuantity(), material.getMinThreshold()
                    ));
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                    
                    String title = isCritical ? 
                        "🚨 CRITICAL: " + material.getName() + " OUT OF STOCK!" :
                        "⚠️ Low Stock: " + material.getName();
                    
                    String message = String.format(
                        "%s is at %d units (Threshold: %d)",
                        material.getName(), material.getQuantity(), material.getMinThreshold()
                    );
                    
                    sendPushNotification(title, message, 
                        isCritical ? "max" : "high", 
                        isCritical ? "warning,skull" : "warning"
                    ).subscribe();
                    
                    alertsSent++;
                    alertSummary.append("• ").append(material.getName())
                        .append(": ").append(material.getQuantity())
                        .append(" units\n");
                }
            }
        }
        
        if (alertsSent > 0) {
            System.out.println("📢 Sent " + alertsSent + " stock alert(s) via Telegram!");
            System.out.println("Alerts:\n" + alertSummary.toString());
        } else {
            System.out.println("✅ No stock alerts needed at this time.");
        }
    }
    
    // =============================================
    // USER MANAGEMENT METHODS
    // =============================================
    
    public void addUser(String email, String chatId) {
        UserSettings user = new UserSettings(email, chatId);
        userSettingsRepository.save(user);
        System.out.println("✅ User added: " + email + " (Chat ID: " + chatId + ")");
        sendTelegramMessageToChat(getWelcomeMessage(), chatId);
    }
    
    public void removeUser(String email) {
        UserSettings user = userSettingsRepository.findByUserEmail(email).orElse(null);
        if (user != null) {
            user.setIsActive(false);
            userSettingsRepository.save(user);
            System.out.println("❌ User deactivated: " + email);
        } else {
            System.out.println("⚠️ User not found: " + email);
        }
    }
    
    public void updateUserPreferences(String email, Boolean notifyLowStock, Boolean notifyCriticalStock, 
                                       Boolean notifyProduction, Boolean notifySales, Boolean notifyDailySummary) {
        UserSettings user = userSettingsRepository.findByUserEmail(email).orElse(null);
        if (user != null) {
            if (notifyLowStock != null) user.setNotifyLowStock(notifyLowStock);
            if (notifyCriticalStock != null) user.setNotifyCriticalStock(notifyCriticalStock);
            if (notifyProduction != null) user.setNotifyProduction(notifyProduction);
            if (notifySales != null) user.setNotifySales(notifySales);
            if (notifyDailySummary != null) user.setNotifyDailySummary(notifyDailySummary);
            userSettingsRepository.save(user);
            System.out.println("✅ Preferences updated for: " + email);
        } else {
            System.out.println("⚠️ User not found: " + email);
        }
    }
}