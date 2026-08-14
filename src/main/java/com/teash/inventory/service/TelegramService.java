/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.service;

/**
 *
 * @author hp
 */


import org.springframework.stereotype.Service;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class TelegramService {
    
    // Replace with your bot token from BotFather
    private static final String BOT_TOKEN = "8923786962:AAFvYvftWd_j-glc2GaQ8nZpht11XMbKGSg";
    
    // Replace with your chat ID from above
    private static final String CHAT_ID = "8687453996";
    
    /**
     * Send notification to Telegram
     */
    public void sendTelegramMessage(String message) {
        sendTelegramMessage(message, CHAT_ID);
    }
    
    /**
     * Send notification to any chat (for family members)
     */
    public void sendTelegramMessage(String message, String chatId) {
        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Escape special characters in message
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
                System.out.println("📱 Telegram notification sent!");
            } else {
                System.out.println("❌ Telegram failed. Response: " + responseCode);
                // Read error response
                try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream())) {
                    s.useDelimiter("\\A");
                    String error = s.hasNext() ? s.next() : "";
                    System.out.println("Error: " + error);
                }
            }
            conn.disconnect();
            
        } catch (Exception e) {
            System.err.println("❌ Telegram error: " + e.getMessage());
        }
    }
    
    /**
     * Send formatted stock alert
     */
    public void sendStockAlert(String material, int quantity, int threshold, boolean isCritical) {
        String emoji = isCritical ? "🚨" : "⚠️";
        String status = isCritical ? "CRITICAL - OUT OF STOCK!" : "LOW STOCK";
        String message = String.format(
            "%s <b>%s</b>\n" +
            "Material: %s\n" +
            "Quantity: <b>%d</b>\n" +
            "Threshold: %d\n" +
            "Time: %s",
            emoji, status, material, quantity, threshold, 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
        );
        
        sendTelegramMessage(message);
    }
}