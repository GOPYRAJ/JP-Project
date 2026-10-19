package com.smartcampus.parcel.pickup.service;

import com.smartcampus.parcel.pickup.dao.NotificationDAO;
import com.smartcampus.parcel.pickup.dao.NotificationDAOImpl;
import com.smartcampus.parcel.pickup.model.Notification;

import java.util.Collections;
import java.util.List;

/**
 * NotificationService handles sending alerts and updates to users.
 * Implements real notification dispatch, retrieval, and status management.
 */
public class NotificationService {

    private final NotificationDAO notificationDAO;

    /**
     * Default constructor initializing default NotificationDAOImpl.
     */
    public NotificationService() {
        this(new NotificationDAOImpl());
    }

    /**
     * Constructor allowing dependency injection of NotificationDAO.
     *
     * @param notificationDAO DAO implementation
     */
    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    /**
     * Sends a notification message to a user regarding a parcel.
     * Preserved signature called across modules. Checks that the recipient user exists,
     * inserts a row in the database, and never throws to the caller.
     *
     * @param userId   recipient user ID
     * @param parcelId relevant parcel ID (use <= 0 or null if not applicable)
     * @param message  notification text
     */
    public void send(int userId, int parcelId, String message) {
        try {
            if (userId <= 0) {
                System.err.println("[NotificationService] Invalid user ID: " + userId);
                return;
            }

            if (!notificationDAO.userExists(userId)) {
                System.err.printf("[NotificationService] Failed to send notification: User #%d does not exist.%n", userId);
                return;
            }

            if (message == null || message.trim().isEmpty()) {
                System.err.println("[NotificationService] Failed to send notification: Message cannot be empty.");
                return;
            }

            Integer pid = (parcelId > 0) ? parcelId : null;
            Notification notification = new Notification(userId, pid, message.trim());
            boolean success = notificationDAO.addNotification(notification);

            if (success) {
                System.out.printf("[NotificationService] Notification dispatched to User #%d (Notification #%d).%n",
                        userId, notification.getNotificationId());
            } else {
                System.err.printf("[NotificationService] Failed to save notification for User #%d into database.%n", userId);
            }
        } catch (Exception e) {
            System.err.printf("[NotificationService] Unexpected error sending notification to User #%d: %s%n",
                    userId, e.getMessage());
        }
    }

    /**
     * Menu-friendly helper to validate and send a notification with a formatted status message.
     *
     * @param userId   recipient user ID
     * @param parcelId relevant parcel ID (optional)
     * @param message  notification text
     * @return status message for UI presentation
     */
    public String sendNotification(int userId, Integer parcelId, String message) {
        if (userId <= 0) {
            return "ERROR: User ID must be a positive integer.";
        }

        if (!notificationDAO.userExists(userId)) {
            return "ERROR: User #" + userId + " does not exist in the system.";
        }

        if (message == null || message.trim().isEmpty()) {
            return "ERROR: Notification message cannot be empty.";
        }

        Integer pid = (parcelId != null && parcelId > 0) ? parcelId : null;
        Notification notification = new Notification(userId, pid, message.trim());
        boolean success = notificationDAO.addNotification(notification);

        if (success) {
            return "SUCCESS: Notification #" + notification.getNotificationId() + " sent to User #" + userId + ".";
        } else {
            return "ERROR: Failed to save notification to database.";
        }
    }

    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId recipient user ID
     * @return list of Notification entities
     */
    public List<Notification> getForUser(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        return notificationDAO.getNotificationsByUser(userId);
    }

    /**
     * Retrieves all unread notifications for a specific user.
     *
     * @param userId recipient user ID
     * @return list of unread Notification entities
     */
    public List<Notification> getUnread(int userId) {
        if (userId <= 0) {
            return Collections.emptyList();
        }
        return notificationDAO.getUnreadByUser(userId);
    }

    /**
     * Marks a specific notification as read.
     *
     * @param notificationId ID of notification to mark as read
     * @return status message for UI presentation
     */
    public String markRead(int notificationId) {
        Notification notif = notificationDAO.getNotificationById(notificationId);
        if (notif == null) {
            return "ERROR: Notification #" + notificationId + " not found.";
        }

        if (notif.isRead()) {
            return "INFO: Notification #" + notificationId + " is already marked as read.";
        }

        boolean updated = notificationDAO.markAsRead(notificationId);
        if (updated) {
            return "SUCCESS: Notification #" + notificationId + " marked as read.";
        } else {
            return "ERROR: Failed to update notification #" + notificationId + ".";
        }
    }

    /**
     * Marks all unread notifications for a user as read.
     *
     * @param userId recipient user ID
     * @return status message for UI presentation
     */
    public String markAllRead(int userId) {
        if (userId <= 0) {
            return "ERROR: Invalid user ID.";
        }

        int unreadCount = notificationDAO.countUnread(userId);
        if (unreadCount == 0) {
            return "INFO: No unread notifications found for User #" + userId + ".";
        }

        boolean updated = notificationDAO.markAllAsRead(userId);
        if (updated) {
            return "SUCCESS: All " + unreadCount + " unread notification(s) marked as read for User #" + userId + ".";
        } else {
            return "ERROR: Failed to update notifications for User #" + userId + ".";
        }
    }

    /**
     * Deletes a notification by its ID.
     *
     * @param notificationId ID of notification to delete
     * @return status message for UI presentation
     */
    public String delete(int notificationId) {
        Notification notif = notificationDAO.getNotificationById(notificationId);
        if (notif == null) {
            return "ERROR: Notification #" + notificationId + " not found.";
        }

        boolean deleted = notificationDAO.deleteNotification(notificationId);
        if (deleted) {
            return "SUCCESS: Notification #" + notificationId + " deleted successfully.";
        } else {
            return "ERROR: Failed to delete notification #" + notificationId + ".";
        }
    }

    /**
     * Counts unread notifications for a specific user.
     *
     * @param userId recipient user ID
     * @return count of unread notifications
     */
    public int countUnread(int userId) {
        if (userId <= 0) {
            return 0;
        }
        return notificationDAO.countUnread(userId);
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param userId recipient user ID
     * @return true if user exists, false otherwise
     */
    public boolean userExists(int userId) {
        return notificationDAO.userExists(userId);
    }
}
