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
    
    private static final String TELEGRAM_BOT_TOKEN = "1234567890:ABCdefGHIjklMNOpqrsTUVwxyz";
    private static final String DEFAULT_CHAT_ID = "987654321";
    
    // =============================================
    // NTFY CONFIGURATION
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
                System.out.println("✅ Telegram message sent to: " + chatId);
            } else {
                System.err.println("❌ Telegram failed: " + responseCode);
            }
            conn.disconnect();
            
        } catch (Exception e) {
            System.err.println("❌ Telegram error: " + e.getMessage());
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
            isCritical ? "🔴 <b>ACTION REQUIRED!</b>" : "🟡 Please check inventory."
        );
        
        sendTelegramMessageToChat(message, chatId);
    }
    
    // =============================================
    // SALES ALERT METHODS
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
    
    // =============================================
    // TEST METHODS
    // =============================================
    
    public void testTelegramConnection() {
        String testMessage = "✅ <b>Connection Test Successful!</b>\n\n" +
            "Your Honey Inventory System is connected to Telegram!\n" +
            "📅 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        List<UserSettings> users = userSettingsRepository.findByIsActiveTrue();
        if (users.isEmpty()) {
            sendTelegramMessageToChat(testMessage, DEFAULT_CHAT_ID);
        } else {
            for (UserSettings user : users) {
                sendTelegramMessageToChat(testMessage, user.getTelegramChatId());
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
                response.put("success", true);
                return response;
            })
            .onErrorResume(e -> {
                response.put("success", false);
                return Mono.just(response);
            });
    }
    
    // =============================================
    // MAIN STOCK CHECK & ALERT METHOD
    // =============================================
    
    public void checkAndSendAlerts() {
        List<RawMaterial> materials = rawMaterialRepository.findAll();
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
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
                }
            }
        }
    }
}