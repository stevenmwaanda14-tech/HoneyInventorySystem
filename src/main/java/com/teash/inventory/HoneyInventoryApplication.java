/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package com.teash.inventory;

/**
 *
 * @author hp
 */



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@SpringBootApplication
@EnableScheduling
public class HoneyInventoryApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HoneyInventoryApplication.class, args);
        System.out.println("🍯 Honey Inventory System Started!");
        System.out.println("📊 API available at: /api");
        System.out.println("📱 Install ntfy app and subscribe to: honey-inventory-alerts");
        System.out.println("✅ All systems ready!");
        
        // Test internet connection
        try {
            InetAddress address = InetAddress.getByName("ntfy.sh");
            System.out.println("✅ ntfy.sh IP: " + address.getHostAddress());
            System.out.println("✅ Connection test: SUCCESS");
        } catch (Exception e) {
            System.out.println("❌ Cannot reach ntfy.sh: " + e.getMessage());
            System.out.println("🔧 This is why notifications are failing!");
        }
    }
}