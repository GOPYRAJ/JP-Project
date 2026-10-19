package com.smartcampus.parcel.pickup.ui;

import com.smartcampus.parcel.pickup.model.Notification;
import com.smartcampus.parcel.pickup.model.PickupPoint;
import com.smartcampus.parcel.pickup.service.NotificationService;
import com.smartcampus.parcel.pickup.service.PickupPointService;
import com.smartcampus.parcel.util.InputUtil;

import java.util.List;

/**
 * PickupMenu provides console-based user interface for Module 4
 * (Pickup Points & Notifications Management).
 */
public class PickupMenu {

    private final PickupPointService pickupPointService;
    private final NotificationService notificationService;

    /**
     * Default constructor initializing default services.
     */
    public PickupMenu() {
        this.pickupPointService = new PickupPointService();
        this.notificationService = new NotificationService();
    }

    /**
     * Constructor allowing dependency injection of services.
     *
     * @param pickupPointService  pickup point service
     * @param notificationService notification service
     */
    public PickupMenu(PickupPointService pickupPointService, NotificationService notificationService) {
        this.pickupPointService = pickupPointService;
        this.notificationService = notificationService;
    }

    /**
     * Displays the interactive menu loop for Module 4.
     */
    public void show() {
        boolean active = true;
        while (active) {
            printMenuHeader();
            int choice = InputUtil.readInt("Enter your choice (0-11)", 0, 11);

            switch (choice) {
                case 1:
                    handleAddPickupPoint();
                    break;
                case 2:
                    handleViewAllPickupPoints();
                    break;
                case 3:
                    handleViewActivePickupPoints();
                    break;
                case 4:
                    handleUpdatePickupPoint();
                    break;
                case 5:
                    handleDeletePickupPoint();
                    break;
                case 6:
                    handleToggleActivePickupPoint();
                    break;
                case 7:
                    handleSendNotification();
                    break;
                case 8:
                    handleViewUserNotifications();
                    break;
                case 9:
                    handleViewUnreadNotifications();
                    break;
                case 10:
                    handleMarkAsRead();
                    break;
                case 11:
                    handleDeleteNotification();
                    break;
                case 0:
                    active = false;
                    System.out.println("\nReturning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option from 0 to 11.");
            }
        }
    }

    /**
     * Prints the Module 4 console menu header.
     */
    private void printMenuHeader() {
        System.out.println("\n===============================================================");
        System.out.println("   PICKUP POINTS & NOTIFICATIONS MODULE (Module 4)            ");
        System.out.println("===============================================================");
        System.out.println("--- PICKUP POINTS ---");
        System.out.println("  1. Add pickup point");
        System.out.println("  2. View all pickup points");
        System.out.println("  3. View active pickup points");
        System.out.println("  4. Update pickup point");
        System.out.println("  5. Delete pickup point");
        System.out.println("  6. Activate / Deactivate pickup point");
        System.out.println("--- NOTIFICATIONS ---");
        System.out.println("  7. Send a notification");
        System.out.println("  8. View a user's notifications");
        System.out.println("  9. View unread notifications");
        System.out.println(" 10. Mark notification as read");
        System.out.println(" 11. Delete a notification");
        System.out.println("  0. Back to Main Menu");
        System.out.println("---------------------------------------------------------------");
    }

    // ==========================================
    // PICKUP POINTS HANDLERS
    // ==========================================

    /**
     * Handles adding a new pickup point.
     */
    private void handleAddPickupPoint() {
        System.out.println("\n--- [1] Add Pickup Point ---");
        String name = InputUtil.readString("Enter Pickup Point Name");
        String location = InputUtil.readString("Enter Location Description");
        int capacity = readPositiveInt("Enter Capacity (> 0)");

        PickupPoint point = new PickupPoint(name, location, capacity, true);
        String result = pickupPointService.addPickupPoint(point);
        System.out.println("\n>>> " + result);
    }

    /**
     * Displays all pickup points in a formatted table.
     */
    private void handleViewAllPickupPoints() {
        System.out.println("\n--- [2] View All Pickup Points ---");
        List<PickupPoint> points = pickupPointService.getAllPickupPoints();
        displayPickupPointTable(points);
    }

    /**
     * Displays only active pickup points.
     */
    private void handleViewActivePickupPoints() {
        System.out.println("\n--- [3] View Active Pickup Points ---");
        List<PickupPoint> points = pickupPointService.getActivePickupPoints();
        displayPickupPointTable(points);
    }

    /**
     * Handles updating an existing pickup point's details.
     */
    private void handleUpdatePickupPoint() {
        System.out.println("\n--- [4] Update Pickup Point ---");
        int pointId = InputUtil.readInt("Enter Pickup Point ID to update");
        PickupPoint existing = pickupPointService.getPickupPointById(pointId);

        if (existing == null) {
            System.out.printf(">>> Pickup point with ID #%d not found.%n", pointId);
            return;
        }

        System.out.println("\nCurrent Details:");
        displayPickupPointDetails(existing);
        System.out.println("(Press Enter to keep current value)");

        String nameInput = InputUtil.readString("Name [" + existing.getName() + "]", true);
        String name = nameInput.isEmpty() ? existing.getName() : nameInput;

        String locInput = InputUtil.readString("Location [" + existing.getLocation() + "]", true);
        String location = locInput.isEmpty() ? existing.getLocation() : locInput;

        int capacity = existing.getCapacity();
        while (true) {
            String capInput = InputUtil.readString("Capacity [" + existing.getCapacity() + "]", true);
            if (capInput.isEmpty()) {
                break;
            }
            try {
                int parsed = Integer.parseInt(capInput);
                if (parsed > 0) {
                    capacity = parsed;
                    break;
                } else {
                    System.out.println("  [!] Capacity must be a positive integer greater than 0.");
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Please enter a valid integer.");
            }
        }

        existing.setName(name);
        existing.setLocation(location);
        existing.setCapacity(capacity);

        String result = pickupPointService.updatePickupPoint(existing);
        System.out.println("\n>>> " + result);
    }

    /**
     * Handles deleting a pickup point, offering deactivation if active parcels remain.
     */
    private void handleDeletePickupPoint() {
        System.out.println("\n--- [5] Delete Pickup Point ---");
        int pointId = InputUtil.readInt("Enter Pickup Point ID to delete");
        PickupPoint existing = pickupPointService.getPickupPointById(pointId);

        if (existing == null) {
            System.out.printf(">>> Pickup point with ID #%d not found.%n", pointId);
            return;
        }

        int activeParcels = pickupPointService.getActiveParcelCount(pointId);
        if (activeParcels > 0) {
            System.out.printf(">>> Cannot delete '%s' (#%d): It currently holds %d active parcel(s).%n",
                    existing.getName(), pointId, activeParcels);
            String ans = InputUtil.readString("Would you like to DEACTIVATE this pickup point instead? (y/n)");
            if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
                String deactivateResult = pickupPointService.setActive(pointId, false);
                System.out.println("\n>>> " + deactivateResult);
            } else {
                System.out.println(">>> Deletion aborted.");
            }
            return;
        }

        String confirm = InputUtil.readString("Are you sure you want to permanently delete '" + existing.getName() + "' (#" + pointId + ")? (y/n)");
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            String result = pickupPointService.deletePickupPoint(pointId);
            System.out.println("\n>>> " + result);
        } else {
            System.out.println(">>> Deletion cancelled.");
        }
    }

    /**
     * Handles toggling the active status of a pickup point.
     */
    private void handleToggleActivePickupPoint() {
        System.out.println("\n--- [6] Activate / Deactivate Pickup Point ---");
        int pointId = InputUtil.readInt("Enter Pickup Point ID");
        PickupPoint existing = pickupPointService.getPickupPointById(pointId);

        if (existing == null) {
            System.out.printf(">>> Pickup point with ID #%d not found.%n", pointId);
            return;
        }

        System.out.printf("Pickup point: %s (Currently: %s)%n",
                existing.getName(), existing.isActive() ? "ACTIVE" : "INACTIVE");
        System.out.println("Select option:");
        System.out.println("  1. Activate");
        System.out.println("  2. Deactivate");
        System.out.println("  0. Cancel");
        int opt = InputUtil.readInt("Choice (0-2)", 0, 2);

        if (opt == 1) {
            String res = pickupPointService.setActive(pointId, true);
            System.out.println("\n>>> " + res);
        } else if (opt == 2) {
            String res = pickupPointService.setActive(pointId, false);
            System.out.println("\n>>> " + res);
        } else {
            System.out.println(">>> Operation cancelled.");
        }
    }

    // ==========================================
    // NOTIFICATIONS HANDLERS
    // ==========================================

    /**
     * Handles sending a new notification to a recipient user.
     */
    private void handleSendNotification() {
        System.out.println("\n--- [7] Send a Notification ---");
        int userId = InputUtil.readInt("Enter Recipient User ID");

        if (!notificationService.userExists(userId)) {
            System.out.printf(">>> User with ID #%d does not exist.%n", userId);
            return;
        }

        String parcelStr = InputUtil.readString("Enter Related Parcel ID (optional, press Enter to skip)", true);
        Integer parcelId = null;
        if (!parcelStr.isEmpty()) {
            try {
                int pid = Integer.parseInt(parcelStr);
                if (pid > 0) {
                    parcelId = pid;
                }
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid parcel ID format. Proceeding without parcel ID.");
            }
        }

        String message = InputUtil.readString("Enter Notification Message");
        String result = notificationService.sendNotification(userId, parcelId, message);
        System.out.println("\n>>> " + result);
    }

    /**
     * Handles viewing all notifications for a given user.
     */
    private void handleViewUserNotifications() {
        System.out.println("\n--- [8] View User's Notifications ---");
        int userId = InputUtil.readInt("Enter User ID");

        if (!notificationService.userExists(userId)) {
            System.out.printf(">>> User with ID #%d does not exist.%n", userId);
            return;
        }

        List<Notification> notifications = notificationService.getForUser(userId);
        displayNotificationTable(notifications, "All Notifications for User #" + userId);
    }

    /**
     * Handles viewing unread notifications for a user, with option to mark all as read.
     */
    private void handleViewUnreadNotifications() {
        System.out.println("\n--- [9] View Unread Notifications ---");
        int userId = InputUtil.readInt("Enter User ID");

        if (!notificationService.userExists(userId)) {
            System.out.printf(">>> User with ID #%d does not exist.%n", userId);
            return;
        }

        List<Notification> unread = notificationService.getUnread(userId);
        displayNotificationTable(unread, "Unread Notifications for User #" + userId);

        if (!unread.isEmpty()) {
            String ans = InputUtil.readString("\nMark all of these notifications as read? (y/n)");
            if (ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
                String result = notificationService.markAllRead(userId);
                System.out.println(">>> " + result);
            }
        }
    }

    /**
     * Handles marking a single notification as read.
     */
    private void handleMarkAsRead() {
        System.out.println("\n--- [10] Mark Notification as Read ---");
        int notifId = InputUtil.readInt("Enter Notification ID");
        String result = notificationService.markRead(notifId);
        System.out.println("\n>>> " + result);
    }

    /**
     * Handles deleting a notification.
     */
    private void handleDeleteNotification() {
        System.out.println("\n--- [11] Delete a Notification ---");
        int notifId = InputUtil.readInt("Enter Notification ID to delete");

        String confirm = InputUtil.readString("Are you sure you want to delete notification #" + notifId + "? (y/n)");
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            String result = notificationService.delete(notifId);
            System.out.println("\n>>> " + result);
        } else {
            System.out.println(">>> Deletion cancelled.");
        }
    }

    // ==========================================
    // DISPLAY & FORMATTING HELPERS
    // ==========================================

    /**
     * Displays a formatted table of pickup points.
     */
    private void displayPickupPointTable(List<PickupPoint> points) {
        if (points == null || points.isEmpty()) {
            System.out.println(">>> No pickup points found.");
            return;
        }

        System.out.println("+-----+--------------------------------+--------------------------------+----------+----------+------------+");
        System.out.printf("| %-3s | %-30s | %-30s | %-8s | %-8s | %-10s |%n",
                "ID", "Name", "Location", "Capacity", "Active", "Status");
        System.out.println("+-----+--------------------------------+--------------------------------+----------+----------+------------+");

        for (PickupPoint p : points) {
            int activeCount = pickupPointService.getActiveParcelCount(p.getPointId());
            String name = truncate(p.getName(), 30);
            String loc = truncate(p.getLocation(), 30);
            String status = p.isActive() ? "ACTIVE" : "INACTIVE";

            System.out.printf("| %-3d | %-30s | %-30s | %-8d | %-8d | %-10s |%n",
                    p.getPointId(), name, loc, p.getCapacity(), activeCount, status);
        }
        System.out.println("+-----+--------------------------------+--------------------------------+----------+----------+------------+");
        System.out.printf("Total Pickup Points: %d%n", points.size());
    }

    /**
     * Formats and prints details for a single pickup point.
     */
    private void displayPickupPointDetails(PickupPoint point) {
        int activeCount = pickupPointService.getActiveParcelCount(point.getPointId());
        boolean hasSpace = pickupPointService.hasFreeCapacity(point.getPointId());

        System.out.println("---------------------------------------------------------------");
        System.out.printf("  Point ID       : %d%n", point.getPointId());
        System.out.printf("  Name           : %s%n", point.getName());
        System.out.printf("  Location       : %s%n", point.getLocation());
        System.out.printf("  Capacity       : %d%n", point.getCapacity());
        System.out.printf("  Active Parcels : %d%n", activeCount);
        System.out.printf("  Free Space?    : %s%n", hasSpace ? "YES" : "NO / FULL");
        System.out.printf("  Status         : %s%n", point.isActive() ? "ACTIVE" : "INACTIVE");
        System.out.println("---------------------------------------------------------------");
    }

    /**
     * Displays a formatted table of notifications.
     */
    private void displayNotificationTable(List<Notification> notifications, String title) {
        System.out.println("\n" + title);
        if (notifications == null || notifications.isEmpty()) {
            System.out.println(">>> No notifications found.");
            return;
        }

        System.out.println("+-----+---------+-----------+--------+---------------------+--------------------------------------------------+");
        System.out.printf("| %-3s | %-7s | %-9s | %-6s | %-19s | %-48s |%n",
                "ID", "User ID", "Parcel ID", "Status", "Timestamp", "Message");
        System.out.println("+-----+---------+-----------+--------+---------------------+--------------------------------------------------+");

        for (Notification n : notifications) {
            String parcelStr = (n.getParcelId() != null) ? String.valueOf(n.getParcelId()) : "-";
            String readStatus = n.isRead() ? "READ" : "UNREAD";
            String ts = n.getCreatedAt() != null ? n.getCreatedAt().toString().substring(0, Math.min(19, n.getCreatedAt().toString().length())) : "-";
            String msg = truncate(n.getMessage(), 48);

            System.out.printf("| %-3d | %-7d | %-9s | %-6s | %-19s | %-48s |%n",
                    n.getNotificationId(), n.getUserId(), parcelStr, readStatus, ts, msg);
        }
        System.out.println("+-----+---------+-----------+--------+---------------------+--------------------------------------------------+");
        System.out.printf("Total Notifications: %d%n", notifications.size());
    }

    /**
     * Safe helper to read a positive integer (> 0).
     */
    private int readPositiveInt(String prompt) {
        while (true) {
            int val = InputUtil.readInt(prompt);
            if (val > 0) {
                return val;
            }
            System.out.println("  [!] Value must be greater than 0. Please try again.");
        }
    }

    /**
     * Helper to truncate long strings for table presentation.
     */
    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "-";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 2) + "..";
    }
}
