package com.smartcampus.parcel.pickup.service;

/**
 * NotificationService handles sending alerts and updates to users.
 * Module 4 will implement notification creation, retrieval, and status updates.
 */
public class NotificationService {

    /**
     * Sends a notification message to a user regarding a parcel.
     * Stub method to allow Modules 2 and 3 to notify recipients from day one.
     *
     * @param userId recipient user ID
     * @param parcelId relevant parcel ID (can be 0 or null if not applicable)
     * @param message notification text
     */
    public void send(int userId, int parcelId, String message) {
        // Module 4 will implement inserting the notification record into the database
        System.out.printf("[NotificationService Stub] Sent to User #%d (Parcel #%d): %s%n", userId, parcelId, message);
    }
}
