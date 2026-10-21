package com.smartcampus.parcel;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.util.InputUtil;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Main application entry point for the Smart Campus Parcel & Delivery Management System.
 * Connects to the database and provides the top-level console menu.
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // Database connection test on startup
        testDatabaseConnection();

        // Main console menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputUtil.readInt("Enter your choice (0-4)", 0, 4);

            switch (choice) {
                case 1:
                    userManagementMenu();
                    break;
                case 2:
                    parcelManagementMenu();
                    break;
                case 3:
                    deliveryTrackingMenu();
                    break;
                case 4:
                    pickupPointsAndNotificationsMenu();
                    break;
                case 0:
                    running = false;
                    System.out.println("\nThank you for using Smart Campus Parcel System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void printBanner() {
        System.out.println("===============================================================");
        System.out.println("   SMART CAMPUS PARCEL & DELIVERY MANAGEMENT SYSTEM");
        System.out.println("===============================================================");
    }

    private static void testDatabaseConnection() {
        System.out.println("\n[System Check] Testing database connection...");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully.\n");
            }
        } catch (SQLException e) {
            System.err.println("[System Check] Failed to connect to database: " + e.getMessage());
            System.err.println("[System Check] Please verify db.properties settings and ensure MySQL server is running.\n");
        }
    }

    private static void printMainMenu() {
        System.out.println("---------------------------------------------------------------");
        System.out.println("MAIN MENU:");
        System.out.println("  1. User Management (Module 1)");
        System.out.println("  2. Parcel Management (Module 2)");
        System.out.println("  3. Delivery & Tracking (Module 3)");
        System.out.println("  4. Pickup Points & Notifications (Module 4)");
        System.out.println("  0. Exit");
        System.out.println("---------------------------------------------------------------");
    }

    // Module placeholder methods (will be replaced by module UI menus)
    private static void userManagementMenu() {
        new com.smartcampus.parcel.user.ui.UserMenu().show();
    }

    private static void parcelManagementMenu() {
        System.out.println("\n>>> [Module 2] Parcel Management: Module coming soon.\n");
    }

    private static void deliveryTrackingMenu() {
        new com.smartcampus.parcel.delivery.ui.DeliveryMenu().show();
    }

    private static void pickupPointsAndNotificationsMenu() {
        new com.smartcampus.parcel.pickup.ui.PickupMenu().show();
    }
}
