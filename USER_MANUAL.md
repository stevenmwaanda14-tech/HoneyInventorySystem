# 🍯 Honey Inventory System - User Manual

## Welcome!

Welcome to the Honey Inventory System! This guide will help you and your family manage your honey inventory easily.

---

## 📱 Quick Start

### 1. Access the Web Dashboard
Open your browser and go to:
http://localhost:8080/dashboard.html



text

**What you can do:**
- ✅ View current stock levels
- ✅ Build new packs
- ✅ Sell packs
- ✅ Receive materials
- ✅ See alerts

### 2. Telegram Bot
On your phone:
1. Install Telegram
2. Search for `@honey_inventory_bot`
3. Type `/start`
4. Use commands to check inventory

---

## 📋 Web Dashboard Guide

### Dashboard Overview
When you open the dashboard, you'll see:

| Section | What it shows |
|---------|---------------|
| **Top Stats** | Total packs, materials, low stock, critical items |
| **Raw Materials** | Jars, Stickers, Boxes quantities and status |
| **Finished Goods** | Honey products ready to sell |
| **Alerts** | Warnings for low or out-of-stock items |

### Actions

#### 🏭 Build Packs
1. Click **"Build Packs"** button
2. Select a product
3. Enter quantity
4. Click **"Build Packs"**

#### 💰 Sell Packs
1. Click **"Sell Packs"** button
2. Select a product
3. Enter quantity
4. (Optional) Add customer name
5. Click **"Sell Packs"**

#### 📦 Receive Materials
1. Click **"Receive"** button
2. Select material (Jars, Stickers, Boxes)
3. Enter quantity
4. (Optional) Add supplier name
5. Click **"Receive Materials"**

#### 🔄 Refresh
Click **"Refresh"** to update all data

---

## 🤖 Telegram Bot Commands

| Command | What it does |
|---------|--------------|
| `/start` | Welcome message and commands list |
| `/stock` | Full inventory summary |
| `/materials` | Raw materials only |
| `/finished` | Finished goods only |
| `/alerts` | View active alerts |
| `/status` | Quick status summary |
| `/help` | Show commands |

### Example Usage
/stock

text
**Response:**
📊 FULL INVENTORY SUMMARY
─────────────────────────
🕐 14:30:25

📦 Raw Materials:
• Jars: 500 (🟢 OK)
• Stickers: 200 (🟢 OK)
• Boxes: 45 (🟡 LOW)

📦 Finished Goods:
• Wildflower Honey: 10 packs (🟢 OK)
• Manuka Honey: 5 packs (🟢 OK)
• Clover Honey: 0 packs (🔴 OUT OF STOCK)

text

---

## 🔔 Automatic Notifications

You'll receive Telegram notifications for:

| Event | What you'll see |
|-------|-----------------|
| **Low Stock** | ⚠️ Low Stock: Jars is at 45 units (Threshold: 100) |
| **Critical Stock** | 🚨 CRITICAL: Jars OUT OF STOCK! |
| **Production** | 🏭 Production Complete: Wildflower Honey - 10 packs built |
| **Sales** | 💰 Sale Recorded: Wildflower Honey - 2 packs sold |
| **Daily Summary** | 📊 DAILY INVENTORY SUMMARY (at 9:00 AM) |

---

## ⚙️ Managing Settings

### Change Alert Thresholds
Open your browser and go to:
http://localhost:8080/api/materials/1/threshold?threshold=350

text
(1 = Jars, 2 = Stickers, 3 = Boxes)

### Add Family Members
1. Get their Telegram Chat ID from `@userinfobot`
2. In MySQL Workbench:
```sql
INSERT INTO user_settings (user_email, telegram_chat_id, is_active) 
VALUES ('family@gmail.com', 'CHAT_ID', true);