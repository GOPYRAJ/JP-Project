package com.smartcampus.parcel.delivery.ui;

import com.smartcampus.parcel.delivery.model.Delivery;
import com.smartcampus.parcel.delivery.model.StatusLog;
import com.smartcampus.parcel.delivery.service.DeliveryService;
import com.smartcampus.parcel.util.Constants;
import com.smartcampus.parcel.util.InputUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * DeliveryMenu provides the console-based user interface for Module 3 (Delivery & Tracking).
 * Supports parcel assignment, tracking updates, status transitions, cancellation, and audit history.
 */
public class DeliveryMenu {

    private final DeliveryService deliveryService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Default constructor initializing DeliveryService.
     */
    public DeliveryMenu() {
        this(new DeliveryService());
    }

    /**
     * Constructor allowing dependency injection of DeliveryService.
     *
     * @param deliveryService DeliveryService instance
     */
    public DeliveryMenu(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    /**
     * Displays the interactive menu loop for Module 3.
     */
    public void show() {
        boolean active = true;
        while (active) {
            printMenuHeader();
            int choice = InputUtil.readInt("Enter your choice (0-9)", 0, 9);

            switch (choice) {
                case 1:
                    handleAssignDelivery();
                    break;
                case 2:
                    handleViewAllDeliveries();
                    break;
                case 3:
                    handleViewDeliveriesByAgent();
                    break;
                case 4:
                    handleViewDeliveriesByStatus();
                    break;
                case 5:
                    handleMarkOutForDelivery();
                    break;
                case 6:
                    handleMarkDelivered();
                    break;
                case 7:
                    handleMarkFailed();
                    break;
                case 8:
                    handleCancelAssignment();
                    break;
                case 9:
                    handleViewStatusHistory();
                    break;
                case 0:
                    active = false;
                    System.out.println("\nReturning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option from 0 to 9.");
            }
        }
    }

    /**
     * Prints the console header for the Delivery & Tracking module.
     */
    private void printMenuHeader() {
        System.out.println("\n===============================================================");
        System.out.println("        DELIVERY & TRACKING MODULE (Module 3)                 ");
        System.out.println("===============================================================");
        System.out.println("  1. Assign parcel to agent");
        System.out.println("  2. View all deliveries");
        System.out.println("  3. View deliveries by agent");
        System.out.println("  4. View deliveries by status");
        System.out.println("  5. Mark OUT FOR DELIVERY");
        System.out.println("  6. Mark DELIVERED");
        System.out.println("  7. Mark FAILED (ask for remarks)");
        System.out.println("  8. Cancel assignment");
        System.out.println("  9. View status history of a parcel");
        System.out.println("  0. Back to Main Menu");
        System.out.println("---------------------------------------------------------------");
    }

    /**
     * Handles assigning a parcel to a delivery agent (Option 1).
     */
    private void handleAssignDelivery() {
        System.out.println("\n--- [Option 1] Assign Parcel to Agent ---");
        int parcelId = InputUtil.readInt("Enter Parcel ID");
        int agentId = InputUtil.readInt("Enter Delivery Agent User ID");

        String result = deliveryService.assignDelivery(parcelId, agentId);
        System.out.println(">>> " + result);
    }

    /**
     * Handles viewing all delivery assignments in the system (Option 2).
     */
    private void handleViewAllDeliveries() {
        System.out.println("\n--- [Option 2] All Deliveries ---");
        List<Delivery> deliveries = deliveryService.getAll();
        displayDeliveryTable(deliveries);
    }

    /**
     * Handles viewing deliveries for a specific delivery agent (Option 3).
     */
    private void handleViewDeliveriesByAgent() {
        System.out.println("\n--- [Option 3] View Deliveries by Agent ---");
        int agentId = InputUtil.readInt("Enter Delivery Agent User ID");
        List<Delivery> deliveries = deliveryService.getByAgent(agentId);
        displayDeliveryTable(deliveries);
    }

    /**
     * Handles viewing deliveries filtered by delivery status (Option 4).
     */
    private void handleViewDeliveriesByStatus() {
        System.out.println("\n--- [Option 4] View Deliveries by Status ---");
        System.out.println("Status Options: ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, FAILED");
        String status = InputUtil.readString("Enter Status").trim().toUpperCase();

        List<Delivery> deliveries = deliveryService.getByStatus(status);
        displayDeliveryTable(deliveries);
    }

    /**
     * Handles marking a delivery as OUT FOR DELIVERY (Option 5).
     */
    private void handleMarkOutForDelivery() {
        System.out.println("\n--- [Option 5] Mark OUT FOR DELIVERY ---");
        int deliveryId = InputUtil.readInt("Enter Delivery ID");
        int changedBy = InputUtil.readInt("Enter User ID making this change (0 for system/default)");
        String remarks = InputUtil.readString("Enter Remarks (optional, press Enter to skip)", true);

        Integer changer = changedBy > 0 ? changedBy : null;
        String result = deliveryService.updateDeliveryStatus(deliveryId,
                Constants.DeliveryStatus.OUT_FOR_DELIVERY, changer, remarks);
        System.out.println(">>> " + result);
    }

    /**
     * Handles marking a delivery as DELIVERED (Option 6).
     */
    private void handleMarkDelivered() {
        System.out.println("\n--- [Option 6] Mark DELIVERED ---");
        int deliveryId = InputUtil.readInt("Enter Delivery ID");
        int changedBy = InputUtil.readInt("Enter User ID making this change (0 for system/default)");
        String remarks = InputUtil.readString("Enter Remarks / Handover note (optional, press Enter to skip)", true);

        Integer changer = changedBy > 0 ? changedBy : null;
        String result = deliveryService.updateDeliveryStatus(deliveryId,
                Constants.DeliveryStatus.DELIVERED, changer, remarks);
        System.out.println(">>> " + result);
    }

    /**
     * Handles marking a delivery as FAILED with required remarks (Option 7).
     */
    private void handleMarkFailed() {
        System.out.println("\n--- [Option 7] Mark FAILED ---");
        int deliveryId = InputUtil.readInt("Enter Delivery ID");
        int changedBy = InputUtil.readInt("Enter User ID making this change (0 for system/default)");
        String remarks = InputUtil.readString("Enter Reason for failure / Remarks (required)");

        Integer changer = changedBy > 0 ? changedBy : null;
        String result = deliveryService.updateDeliveryStatus(deliveryId,
                Constants.DeliveryStatus.FAILED, changer, remarks);
        System.out.println(">>> " + result);
    }

    /**
     * Handles cancelling a delivery assignment in ASSIGNED status (Option 8).
     */
    private void handleCancelAssignment() {
        System.out.println("\n--- [Option 8] Cancel Assignment ---");
        int deliveryId = InputUtil.readInt("Enter Delivery ID to cancel");
        int changedBy = InputUtil.readInt("Enter User ID cancelling this delivery (0 for system/default)");

        Integer changer = changedBy > 0 ? changedBy : null;
        String result = deliveryService.cancelDelivery(deliveryId, changer);
        System.out.println(">>> " + result);
    }

    /**
     * Handles viewing the status audit history of a parcel (Option 9).
     */
    private void handleViewStatusHistory() {
        System.out.println("\n--- [Option 9] View Status History of Parcel ---");
        int parcelId = InputUtil.readInt("Enter Parcel ID");

        List<StatusLog> logs = deliveryService.getHistoryForParcel(parcelId);
        displayStatusLogTable(logs);
    }

    /**
     * Formats and prints a list of deliveries in a readable tabular format.
     *
     * @param deliveries list of Delivery objects
     */
    private void displayDeliveryTable(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            System.out.println(">>> No deliveries found.");
            return;
        }

        System.out.println("+-----+-----------+----------+------------------+---------------------+---------------------+--------------------------------+");
        System.out.printf("| %-3s | %-9s | %-8s | %-16s | %-19s | %-19s | %-30s |%n",
                "ID", "Parcel ID", "Agent ID", "Status", "Assigned At", "Delivered At", "Remarks");
        System.out.println("+-----+-----------+----------+------------------+---------------------+---------------------+--------------------------------+");

        for (Delivery d : deliveries) {
            String assignedStr = formatDate(d.getAssignedAt());
            String deliveredStr = formatDate(d.getDeliveredAt());
            String remarksStr = truncate(d.getRemarks(), 30);

            System.out.printf("| %-3d | %-9d | %-8d | %-16s | %-19s | %-19s | %-30s |%n",
                    d.getDeliveryId(),
                    d.getParcelId(),
                    d.getAgentId(),
                    d.getStatus(),
                    assignedStr,
                    deliveredStr,
                    remarksStr);
        }
        System.out.println("+-----+-----------+----------+------------------+---------------------+---------------------+--------------------------------+");
        System.out.printf("Total Deliveries: %d%n", deliveries.size());
    }

    /**
     * Formats and prints a list of status log entries in a readable tabular format.
     *
     * @param logs list of StatusLog objects
     */
    private void displayStatusLogTable(List<StatusLog> logs) {
        if (logs == null || logs.isEmpty()) {
            System.out.println(">>> No status history found for this parcel.");
            return;
        }

        System.out.println("+--------+-----------+------------------+------------+---------------------+--------------------------------------+");
        System.out.printf("| %-6s | %-9s | %-16s | %-10s | %-19s | %-36s |%n",
                "Log ID", "Parcel ID", "Status", "Changed By", "Changed At", "Remarks");
        System.out.println("+--------+-----------+------------------+------------+---------------------+--------------------------------------+");

        for (StatusLog log : logs) {
            String changerStr = (log.getChangedBy() != null && log.getChangedBy() > 0)
                    ? String.valueOf(log.getChangedBy())
                    : "System";
            String changedAtStr = formatDate(log.getChangedAt());
            String remarksStr = truncate(log.getRemarks(), 36);

            System.out.printf("| %-6d | %-9d | %-16s | %-10s | %-19s | %-36s |%n",
                    log.getLogId(),
                    log.getParcelId(),
                    log.getStatus(),
                    changerStr,
                    changedAtStr,
                    remarksStr);
        }
        System.out.println("+--------+-----------+------------------+------------+---------------------+--------------------------------------+");
        System.out.printf("Total Status Log Entries: %d%n", logs.size());
    }

    /**
     * Helper to safely format a Timestamp into a readable string.
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "-";
        }
        return DATE_FORMAT.format(date);
    }

    /**
     * Helper to truncate strings that exceed maximum column width.
     */
    private String truncate(String text, int maxLen) {
        if (text == null || text.trim().isEmpty()) {
            return "-";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 2) + "..";
    }
}
