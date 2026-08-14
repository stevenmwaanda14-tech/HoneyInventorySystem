/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.repository;

/**
 *
 * @author hp
 */


import com.teash.inventory.entity.FinishedGoods;
import com.teash.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinishedGoodsRepository extends JpaRepository<FinishedGoods, Long> {
    Optional<FinishedGoods> findByProduct(Product product);
    List<FinishedGoods> findByQuantityPacksGreaterThan(Integer quantity);
}