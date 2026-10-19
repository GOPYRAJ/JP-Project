package com.smartcampus.parcel.pickup.model;

import java.sql.Timestamp;

/**
 * Notification model class representing a record in the 'notifications' table.
 * Stores alerts and status messages dispatched to campus recipients.
 */
public class Notification {

    private int notificationId;
    private int userId;
    private Integer parcelId;
    private String message;
    private boolean read;
    private Timestamp createdAt;

    /**
     * Default no-argument constructor.
     */
    public Notification() {
    }

    /**
     * Constructor for creating a new notification before database insertion.
     *
     * @param userId   the recipient user ID
     * @param parcelId relevant parcel ID (optional, can be null or <= 0)
     * @param message  notification text
     */
    public Notification(int userId, Integer parcelId, String message) {
        this.userId = userId;
        this.parcelId = (parcelId != null && parcelId > 0) ? parcelId : null;
        this.message = message;
        this.read = false;
    }

    /**
     * Full constructor including all fields from the database.
     *
     * @param notificationId unique notification ID
     * @param userId         the recipient user ID
     * @param parcelId       relevant parcel ID (nullable)
     * @param message        notification text
     * @param read           whether the notification has been read
     * @param createdAt      timestamp when notification was created
     */
    public Notification(int notificationId, int userId, Integer parcelId, String message, boolean read, Timestamp createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.parcelId = (parcelId != null && parcelId > 0) ? parcelId : null;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    /**
     * Gets the notification ID.
     */
    public int getNotificationId() {
        return notificationId;
    }

    /**
     * Sets the notification ID.
     */
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    /**
     * Gets the recipient user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the recipient user ID.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the associated parcel ID (may be null).
     */
    public Integer getParcelId() {
        return parcelId;
    }

    /**
     * Sets the associated parcel ID.
     */
    public void setParcelId(Integer parcelId) {
        this.parcelId = (parcelId != null && parcelId > 0) ? parcelId : null;
    }

    /**
     * Gets the notification message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the notification message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns true if the notification has been read.
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Sets the read status of the notification.
     */
    public void setRead(boolean read) {
        this.read = read;
    }

    /**
     * Gets the timestamp when notification was created.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when notification was created.
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Notification [ID=%d, UserID=%d, ParcelID=%s, Message='%s', Read=%b, CreatedAt=%s]",
                notificationId, userId, (parcelId != null ? String.valueOf(parcelId) : "N/A"),
                message, read, (createdAt != null ? createdAt.toString() : "-"));
    }
}
