-- ====================================================================
-- Smart Campus Parcel & Delivery Management System - Sample Data
-- Database: smart_campus_parcel
-- All passwords are encrypted with SHA-256 for 'password123'
-- Hash: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- ====================================================================

USE smart_campus_parcel;

-- Clean existing data in reverse order of foreign keys
DELETE FROM notifications;
DELETE FROM parcel_status_log;
DELETE FROM deliveries;
DELETE FROM parcels;
DELETE FROM pickup_points;
DELETE FROM users;

-- Reset Auto Increment counters
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE pickup_points AUTO_INCREMENT = 1;
ALTER TABLE parcels AUTO_INCREMENT = 1;
ALTER TABLE deliveries AUTO_INCREMENT = 1;
ALTER TABLE parcel_status_log AUTO_INCREMENT = 1;
ALTER TABLE notifications AUTO_INCREMENT = 1;

-- 1. USERS (At least one of each role: ADMIN, STUDENT, STAFF, DELIVERY_AGENT)
INSERT INTO users (user_id, full_name, email, phone, password_hash, role, department, hostel_block) VALUES
(1, 'Admin User', 'admin@smartcampus.edu', '9876543210', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN', 'Administration', 'Staff Quarters A'),
(2, 'Aarav Sharma', 'aarav.sharma@smartcampus.edu', '9876543211', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Computer Science', 'Hostel Block B'),
(3, 'Priya Patel', 'priya.patel@smartcampus.edu', '9876543212', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STUDENT', 'Electrical Engineering', 'Hostel Block C'),
(4, 'Dr. Vikram Rao', 'vikram.rao@smartcampus.edu', '9876543213', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'STAFF', 'Mechanical Engineering', 'Faculty Tower 1'),
(5, 'Ramesh Kumar', 'ramesh.delivery@smartcampus.edu', '9876543214', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'DELIVERY_AGENT', 'Logistics', 'Campus Hub');

-- 2. PICKUP POINTS
INSERT INTO pickup_points (point_id, name, location, capacity, is_active) VALUES
(1, 'Main Gate Security Hub', 'Campus Main Entrance Gate 1', 100, TRUE),
(2, 'Hostel Block B Locker', 'Ground Floor, Boys Hostel B', 50, TRUE),
(3, 'Hostel Block C Locker', 'Ground Floor, Girls Hostel C', 50, TRUE),
(4, 'Central Library Counter', 'Library Building, West Wing', 40, TRUE);

-- 3. PARCELS
INSERT INTO parcels (parcel_id, tracking_no, sender_name, courier_company, recipient_id, pickup_point_id, weight_kg, description, status, received_at, collected_at) VALUES
(1, 'TRK10001', 'Amazon India', 'BlueDart', 2, 2, 1.50, 'Electronics - Laptop Charger', 'RECEIVED', NOW() - INTERVAL 2 DAY, NULL),
(2, 'TRK10002', 'Flipkart', 'Ekart', 3, 3, 0.85, 'Textbooks - Data Structures', 'ASSIGNED', NOW() - INTERVAL 1 DAY, NULL),
(3, 'TRK10003', 'Myntra', 'Delhivery', 2, 2, 1.20, 'Clothing - Winter Jacket', 'OUT_FOR_DELIVERY', NOW() - INTERVAL 5 HOUR, NULL),
(4, 'TRK10004', 'Wiley Publications', 'DTDC', 4, 4, 2.10, 'Academic Research Journals', 'DELIVERED', NOW() - INTERVAL 1 DAY, NULL),
(5, 'TRK10005', 'Lenovo India', 'BlueDart', 3, 1, 3.50, 'Computer Monitor', 'COLLECTED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 1 DAY);

-- 4. DELIVERIES
INSERT INTO deliveries (delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks) VALUES
(1, 2, 5, 'ASSIGNED', NOW() - INTERVAL 1 DAY, NULL, 'Package scheduled for delivery dispatch'),
(2, 3, 5, 'OUT_FOR_DELIVERY', NOW() - INTERVAL 5 HOUR, NULL, 'Agent en route to Hostel Block B'),
(3, 4, 5, 'DELIVERED', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 4 HOUR, 'Handed over at Central Library Counter'),
(4, 5, 5, 'DELIVERED', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 2 DAY, 'Delivered and confirmed via OTP');

-- 5. PARCEL STATUS LOG
INSERT INTO parcel_status_log (log_id, parcel_id, status, changed_by, changed_at, remarks) VALUES
(1, 1, 'RECEIVED', 1, NOW() - INTERVAL 2 DAY, 'Initial intake at Main Gate Security Hub'),
(2, 2, 'ASSIGNED', 1, NOW() - INTERVAL 1 DAY, 'Assigned to delivery agent Ramesh Kumar'),
(3, 3, 'OUT_FOR_DELIVERY', 5, NOW() - INTERVAL 5 HOUR, 'Agent dispatched with parcel'),
(4, 4, 'DELIVERED', 5, NOW() - INTERVAL 4 HOUR, 'Delivered at Central Library Counter'),
(5, 5, 'COLLECTED', 1, NOW() - INTERVAL 1 DAY, 'Recipient confirmed parcel collection');

-- 6. NOTIFICATIONS
INSERT INTO notifications (notification_id, user_id, parcel_id, message, is_read, created_at) VALUES
(1, 2, 1, 'Your parcel TRK10001 from Amazon India has arrived on campus.', FALSE, NOW() - INTERVAL 2 DAY),
(2, 3, 2, 'Your parcel TRK10002 has been assigned for campus delivery.', FALSE, NOW() - INTERVAL 1 DAY),
(3, 2, 3, 'Your parcel TRK10003 is out for delivery with agent Ramesh Kumar.', FALSE, NOW() - INTERVAL 5 HOUR),
(4, 4, 4, 'Your parcel TRK10004 has been delivered at Central Library Counter.', TRUE, NOW() - INTERVAL 4 HOUR);
