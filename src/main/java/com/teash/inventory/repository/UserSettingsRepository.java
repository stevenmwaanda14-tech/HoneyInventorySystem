/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.teash.inventory.repository;

/**
 *
 * @author hp
 */




import com.teash.inventory.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    
    Optional<UserSettings> findByUserEmail(String userEmail);
    Optional<UserSettings> findByTelegramChatId(String telegramChatId);
    List<UserSettings> findByIsActiveTrue();
    List<UserSettings> findByNotifyDailySummaryTrueAndIsActiveTrue();
    List<UserSettings> findByNotifyLowStockTrueAndIsActiveTrue();
    List<UserSettings> findByNotifyCriticalStockTrueAndIsActiveTrue();
}