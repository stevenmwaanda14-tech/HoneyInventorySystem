/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.config;

/**
 *
 * @author hp
 */

import com.teash.inventory.entity.*;
import com.teash.inventory.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    
    @Autowired
    private RawMaterialRepository rawMaterialRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private FinishedGoodsRepository finishedGoodsRepository;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("📦 Loading seed data...");
        
        // Seed raw materials if empty
        if (rawMaterialRepository.count() == 0) {
            rawMaterialRepository.save(new RawMaterial("Jars", 100));
            rawMaterialRepository.save(new RawMaterial("Stickers", 50));
            rawMaterialRepository.save(new RawMaterial("Boxes", 25));
            System.out.println("✅ Seeded 3 raw materials");
        } else {
            System.out.println("ℹ️ Raw materials already exist, skipping seed");
        }
        
        // Seed products if empty - NO SKU
        if (productRepository.count() == 0) {
            Product squeezable500 = productRepository.save(new Product("Squeezable 500g", 1));
            Product squeezable375 = productRepository.save(new Product("Squeezable 375ml", 1));
            Product ordinary375 = productRepository.save(new Product("Ordinary 375ml", 1));
            
            // Initialize finished goods
            finishedGoodsRepository.save(new FinishedGoods(squeezable500, 10));
            finishedGoodsRepository.save(new FinishedGoods(squeezable375, 5));
            finishedGoodsRepository.save(new FinishedGoods(ordinary375, 0));
            System.out.println("✅ Seeded 3 products:");
            System.out.println("   - Squeezable 500g (10 packs)");
            System.out.println("   - Squeezable 375ml (5 packs)");
            System.out.println("   - Ordinary 375ml (0 packs)");
        } else {
            System.out.println("ℹ️ Products already exist, skipping seed");
        }
        
        System.out.println("🎯 Data loading complete!");
    }
}