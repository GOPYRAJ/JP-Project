package com.smartcampus.parcel.parcel.ui;

import com.smartcampus.parcel.parcel.model.Parcel;
import com.smartcampus.parcel.parcel.service.ParcelService;
import com.smartcampus.parcel.util.Constants;
import com.smartcampus.parcel.util.InputUtil;

import java.math.BigDecimal;
import java.util.List;

/**
 * ParcelMenu provides the console-based user interface for Parcel Management.
 * Reads user input via InputUtil (no direct Scanner usage).
 * Delegates all business logic to ParcelService; contains no SQL.
 * Call show() to enter the parcel management submenu.
 */
public class ParcelMenu {

    private final ParcelService parcelService;

    /** Default constructor used by Main.java (or the team leader's wiring). */
    public ParcelMenu() {
        this.parcelService = new ParcelService();
    }

    /** Constructor for testing / dependency injection. */
    public ParcelMenu(ParcelService parcelService) {
        this.parcelService = parcelService;
    }

    // -------------------------------------------------------
    // Main entry point
    // -------------------------------------------------------

    /**
     * Displays the Parcel Management submenu and handles navigation.
     * Returns to the caller (Main menu) when the user chooses option 9.
     */
    public void show() {
        boolean back = false;
        while (!back) {
            printMenu();
            int choice = InputUtil.readInt("Enter your choice (1-9)", 1, 9);
            switch (choice) {
                case 1 -> registerParcel();
                case 2 -> viewAllParcels();
                case 3 -> searchByTrackingNumber();
                case 4 -> searchByRecipient();
                case 5 -> viewParcelById();
                case 6 -> updateParcelDetails();
                case 7 -> updateParcelStatus();
                case 8 -> deleteParcel();
                case 9 -> back = true;
                default -> System.out.println("  [!] Invalid option. Please try again.");
            }
        }
        System.out.println("\n[Parcel Management] Returning to main menu.\n");
    }

    // -------------------------------------------------------
    // Menu options
    // -------------------------------------------------------

    /** 1. Register a new parcel. */
    private void registerParcel() {
        printSeparator("Register New Parcel");
        try {
            String trackingNo    = InputUtil.readString("  Tracking Number");
            String senderName    = InputUtil.readString("  Sender Name");
            String courierCo     = InputUtil.readString("  Courier Company");
            int recipientId      = InputUtil.readInt("  Recipient User ID");
            int pickupPointId    = InputUtil.readInt("  Pickup Point ID");
            String weightStr     = InputUtil.readString("  Weight in kg (leave blank to skip)", true);
            String description   = InputUtil.readString("  Description (leave blank to skip)", true);
            int registeredBy     = InputUtil.readInt("  Registering Admin User ID");

            BigDecimal weight = null;
            if (!weightStr.isBlank()) {
                try {
                    weight = new BigDecimal(weightStr.trim());
                } catch (NumberFormatException e) {
                    System.out.println("  [!] Invalid weight value. Defaulting to null.");
                }
            }

            int newId = parcelService.registerParcel(trackingNo, senderName, courierCo,
                recipientId, pickupPointId, weight, description.isBlank() ? null : description,
                registeredBy);
            System.out.println("\n  [OK] Parcel registered successfully. Parcel ID: " + newId);
        } catch (IllegalArgumentException e) {
            System.out.println("  [!] Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 2. View all parcels. */
    private void viewAllParcels() {
        printSeparator("All Parcels");
        try {
            List<Parcel> parcels = parcelService.getAllParcels();
            if (parcels.isEmpty()) {
                System.out.println("  No parcels found in the system.");
            } else {
                System.out.printf("  %-6s %-15s %-20s %-18s %-6s %-6s %-18s %s%n",
                    "ID", "Tracking No", "Sender", "Courier", "Recip", "Point", "Status", "Received At");
                printDivider();
                for (Parcel p : parcels) {
                    printParcelRow(p);
                }
                System.out.println("  Total: " + parcels.size() + " parcel(s).");
            }
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 3. Search parcel by tracking number. */
    private void searchByTrackingNumber() {
        printSeparator("Search Parcel by Tracking Number");
        String trackingNo = InputUtil.readString("  Tracking Number");
        try {
            Parcel parcel = parcelService.searchByTrackingNo(trackingNo);
            if (parcel == null) {
                System.out.println("  No parcel found with tracking number: " + trackingNo);
            } else {
                printParcelDetail(parcel);
            }
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 4. Search parcels by recipient user ID. */
    private void searchByRecipient() {
        printSeparator("Search Parcels by Recipient");
        int recipientId = InputUtil.readInt("  Recipient User ID");
        try {
            List<Parcel> parcels = parcelService.getByRecipient(recipientId);
            if (parcels.isEmpty()) {
                System.out.println("  No parcels found for recipient ID: " + recipientId);
            } else {
                System.out.printf("  %-6s %-15s %-20s %-18s %-6s %-18s%n",
                    "ID", "Tracking No", "Sender", "Courier", "Point", "Status");
                printDivider();
                for (Parcel p : parcels) {
                    printParcelRow(p);
                }
                System.out.println("  Total: " + parcels.size() + " parcel(s).");
            }
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 5. View parcel by ID. */
    private void viewParcelById() {
        printSeparator("View Parcel by ID");
        int parcelId = InputUtil.readInt("  Parcel ID");
        try {
            Parcel parcel = parcelService.getParcelById(parcelId);
            if (parcel == null) {
                System.out.println("  No parcel found with ID: " + parcelId);
            } else {
                printParcelDetail(parcel);
            }
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 6. Update parcel details (non-status fields). */
    private void updateParcelDetails() {
        printSeparator("Update Parcel Details");
        int parcelId = InputUtil.readInt("  Parcel ID to update");
        try {
            Parcel existing = parcelService.getParcelById(parcelId);
            if (existing == null) {
                System.out.println("  [!] Parcel ID " + parcelId + " not found.");
                return;
            }
            System.out.println("  Leave a field blank to keep the current value.");
            printParcelDetail(existing);

            String trackingNo   = promptUpdate("  New Tracking Number", existing.getTrackingNo());
            String senderName   = promptUpdate("  New Sender Name", existing.getSenderName());
            String courierCo    = promptUpdate("  New Courier Company", existing.getCourierCompany());

            String recipStr = InputUtil.readString(
                "  New Recipient User ID [current: " + existing.getRecipientId() + "]", true);
            int recipientId = recipStr.isBlank() ? existing.getRecipientId()
                : parseIntSafe(recipStr, existing.getRecipientId());

            String pointStr = InputUtil.readString(
                "  New Pickup Point ID [current: " + existing.getPickupPointId() + "]", true);
            int pickupPointId = pointStr.isBlank() ? existing.getPickupPointId()
                : parseIntSafe(pointStr, existing.getPickupPointId());

            String weightStr = InputUtil.readString(
                "  New Weight kg [current: " + existing.getWeightKg() + "]", true);
            BigDecimal weight = weightStr.isBlank() ? existing.getWeightKg()
                : parseBigDecimalSafe(weightStr, existing.getWeightKg());

            String description = InputUtil.readString(
                "  New Description [current: " + existing.getDescription() + "]", true);
            if (description.isBlank()) description = existing.getDescription();

            existing.setTrackingNo(trackingNo);
            existing.setSenderName(senderName);
            existing.setCourierCompany(courierCo);
            existing.setRecipientId(recipientId);
            existing.setPickupPointId(pickupPointId);
            existing.setWeightKg(weight);
            existing.setDescription(description);

            boolean ok = parcelService.updateParcel(existing);
            System.out.println(ok ? "  [OK] Parcel updated successfully."
                                  : "  [!] Update did not affect any rows.");
        } catch (IllegalArgumentException e) {
            System.out.println("  [!] Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 7. Update parcel status. */
    private void updateParcelStatus() {
        printSeparator("Update Parcel Status");
        System.out.println("  Valid statuses: RECEIVED, ASSIGNED, OUT_FOR_DELIVERY,");
        System.out.println("                  DELIVERED, COLLECTED, FAILED, CANCELLED");
        int parcelId   = InputUtil.readInt("  Parcel ID");
        String status  = InputUtil.readString("  New Status").toUpperCase();
        String remarks = InputUtil.readString("  Remarks (leave blank to skip)", true);
        int changedBy  = InputUtil.readInt("  Your User ID (for audit log)");

        try {
            parcelService.updateParcelStatus(parcelId, status, changedBy,
                remarks.isBlank() ? null : remarks);
            System.out.println("  [OK] Parcel #" + parcelId + " status updated to: " + status);
        } catch (IllegalArgumentException e) {
            System.out.println("  [!] Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    /** 8. Delete a parcel (only RECEIVED or CANCELLED). */
    private void deleteParcel() {
        printSeparator("Delete Parcel");
        int parcelId = InputUtil.readInt("  Parcel ID to delete");
        try {
            Parcel parcel = parcelService.getParcelById(parcelId);
            if (parcel == null) {
                System.out.println("  [!] Parcel ID " + parcelId + " not found.");
                return;
            }
            printParcelDetail(parcel);
            String confirm = InputUtil.readString(
                "\n  Type YES to confirm deletion of parcel #" + parcelId);
            if (!"YES".equalsIgnoreCase(confirm)) {
                System.out.println("  Deletion cancelled.");
                return;
            }
            boolean ok = parcelService.deleteParcel(parcelId);
            System.out.println(ok ? "  [OK] Parcel #" + parcelId + " deleted successfully."
                                  : "  [!] Deletion failed.");
        } catch (IllegalArgumentException e) {
            System.out.println("  [!] Validation error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("  [!] Error: " + e.getMessage());
        }
    }

    // -------------------------------------------------------
    // Display helpers
    // -------------------------------------------------------

    private void printMenu() {
        System.out.println("\n===============================================================");
        System.out.println("          PARCEL MANAGEMENT — MODULE 2");
        System.out.println("===============================================================");
        System.out.println("  1. Register parcel");
        System.out.println("  2. View all parcels");
        System.out.println("  3. Search parcel by tracking number");
        System.out.println("  4. Search parcels by recipient");
        System.out.println("  5. View parcel by ID");
        System.out.println("  6. Update parcel details");
        System.out.println("  7. Update parcel status");
        System.out.println("  8. Delete parcel");
        System.out.println("  9. Return to main menu");
        System.out.println("---------------------------------------------------------------");
    }

    private void printSeparator(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private void printDivider() {
        System.out.println("  " + "-".repeat(90));
    }

    /** Prints a compact single-line summary row for a parcel. */
    private void printParcelRow(Parcel p) {
        System.out.printf("  %-6d %-15s %-20s %-18s %-6d %-6d %-18s %s%n",
            p.getParcelId(),
            truncate(p.getTrackingNo(), 14),
            truncate(p.getSenderName(), 19),
            truncate(p.getCourierCompany(), 17),
            p.getRecipientId(),
            p.getPickupPointId(),
            p.getStatus(),
            p.getReceivedAt() != null ? p.getReceivedAt().toString() : "N/A");
    }

    /** Prints a full detail block for a single parcel. */
    private void printParcelDetail(Parcel p) {
        System.out.println();
        System.out.println("  Parcel ID      : " + p.getParcelId());
        System.out.println("  Tracking No    : " + p.getTrackingNo());
        System.out.println("  Sender         : " + p.getSenderName());
        System.out.println("  Courier        : " + p.getCourierCompany());
        System.out.println("  Recipient ID   : " + p.getRecipientId());
        System.out.println("  Pickup Point   : " + p.getPickupPointId());
        System.out.println("  Weight (kg)    : " + (p.getWeightKg() != null ? p.getWeightKg() : "N/A"));
        System.out.println("  Description    : " + (p.getDescription() != null ? p.getDescription() : "N/A"));
        System.out.println("  Status         : " + p.getStatus());
        System.out.println("  Received At    : " + (p.getReceivedAt() != null ? p.getReceivedAt() : "N/A"));
        System.out.println("  Collected At   : " + (p.getCollectedAt() != null ? p.getCollectedAt() : "N/A"));
    }

    // -------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------

    /**
     * Shows a prompt with the current value and returns the new value,
     * or the current value if the user leaves the field blank.
     */
    private String promptUpdate(String prompt, String currentValue) {
        String input = InputUtil.readString(prompt + " [current: " + currentValue + "]", true);
        return input.isBlank() ? currentValue : input;
    }

    /** Parses an int safely, returning the fallback on parse failure. */
    private int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) {
            System.out.println("  [!] Invalid number, keeping current value.");
            return fallback;
        }
    }

    /** Parses a BigDecimal safely, returning the fallback on parse failure. */
    private BigDecimal parseBigDecimalSafe(String s, BigDecimal fallback) {
        try { return new BigDecimal(s.trim()); }
        catch (NumberFormatException e) {
            System.out.println("  [!] Invalid decimal, keeping current value.");
            return fallback;
        }
    }

    /** Truncates a string to maxLen characters for table alignment. */
    private String truncate(String s, int maxLen) {
        if (s == null) return "N/A";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }
}
