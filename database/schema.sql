-- ====================================================================
-- Smart Campus Parcel & Delivery Management System - Database Schema
-- Database: smart_campus_parcel
-- MySQL 8.x
-- ====================================================================

CREATE DATABASE IF NOT EXISTS smart_campus_parcel;
USE smart_campus_parcel;

-- Drop child tables first if they exist to avoid FK constraint issues during resets
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS parcel_status_log;
DROP TABLE IF EXISTS deliveries;
DROP TABLE IF EXISTS parcels;
DROP TABLE IF EXISTS pickup_points;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE (Module 1)
-- Stores credentials and profile information for all campus members
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(100) NOT NULL,
    email          VARCHAR(100) NOT NULL UNIQUE,
    phone          VARCHAR(15)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           ENUM('STUDENT', 'STAFF', 'ADMIN', 'DELIVERY_AGENT') NOT NULL,
    department     VARCHAR(100),
    hostel_block   VARCHAR(50),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. PICKUP POINTS TABLE (Module 4)
-- Campus physical collection locations / lockers
CREATE TABLE pickup_points (
    point_id    INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    location    VARCHAR(200) NOT NULL,
    capacity    INT NOT NULL CHECK (capacity > 0),
    is_active   BOOLEAN DEFAULT TRUE
);

-- 3. PARCELS TABLE (Module 2)
-- Incoming parcels received at campus gates or hubs
CREATE TABLE parcels (
    parcel_id        INT AUTO_INCREMENT PRIMARY KEY,
    tracking_no      VARCHAR(50)  NOT NULL UNIQUE,
    sender_name      VARCHAR(100) NOT NULL,
    courier_company  VARCHAR(100) NOT NULL,
    recipient_id     INT NOT NULL,
    pickup_point_id  INT NOT NULL,
    weight_kg        DECIMAL(6,2),
    description      VARCHAR(255),
    status           ENUM('RECEIVED', 'ASSIGNED', 'OUT_FOR_DELIVERY',
                          'DELIVERED', 'COLLECTED', 'FAILED', 'CANCELLED')
                     DEFAULT 'RECEIVED',
    received_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    collected_at     TIMESTAMP NULL,
    FOREIGN KEY (recipient_id)    REFERENCES users(user_id),
    FOREIGN KEY (pickup_point_id) REFERENCES pickup_points(point_id)
);

-- 4. DELIVERIES TABLE (Module 3)
-- Assignments of parcels to delivery agents
CREATE TABLE deliveries (
    delivery_id   INT AUTO_INCREMENT PRIMARY KEY,
    parcel_id     INT NOT NULL,
    agent_id      INT NOT NULL,
    status        ENUM('ASSIGNED', 'OUT_FOR_DELIVERY', 'DELIVERED', 'FAILED')
                  DEFAULT 'ASSIGNED',
    assigned_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at  TIMESTAMP NULL,
    remarks       VARCHAR(255),
    FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id)  REFERENCES users(user_id)
);

-- 5. PARCEL STATUS LOG TABLE (Module 3)
-- Audit history tracking every state change of a parcel
CREATE TABLE parcel_status_log (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    parcel_id    INT NOT NULL,
    status       VARCHAR(30) NOT NULL,
    changed_by   INT,
    changed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    remarks      VARCHAR(255),
    FOREIGN KEY (parcel_id)  REFERENCES parcels(parcel_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- 6. NOTIFICATIONS TABLE (Module 4)
-- Alerts and notifications sent to students/staff regarding their parcels
CREATE TABLE notifications (
    notification_id  INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    parcel_id        INT,
    message          VARCHAR(255) NOT NULL,
    is_read          BOOLEAN DEFAULT FALSE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)   REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parcel_id) REFERENCES parcels(parcel_id) ON DELETE SET NULL
);
