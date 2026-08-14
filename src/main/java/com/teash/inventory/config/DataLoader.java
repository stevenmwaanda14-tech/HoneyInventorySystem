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
        }
        
        // Seed products if empty
        if (productRepository.count() == 0) {
            Product wildflower = productRepository.save(new Product("Wildflower Honey", "HNY-WF-001"));
            Product manuka = productRepository.save(new Product("Manuka Honey", "HNY-MK-001"));
            Product clover = productRepository.save(new Product("Clover Honey", "HNY-CL-001"));
            
            // Initialize finished goods
            finishedGoodsRepository.save(new FinishedGoods(wildflower, 10));
            finishedGoodsRepository.save(new FinishedGoods(manuka, 5));
            finishedGoodsRepository.save(new FinishedGoods(clover, 0));
            System.out.println("✅ Seeded 3 products");
        }
        
        System.out.println("🎯 Data loading complete!");
    }
}