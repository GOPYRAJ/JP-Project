package com.smartcampus.parcel.pickup.dao;

import com.smartcampus.parcel.pickup.model.Notification;

import java.util.List;

/**
 * NotificationDAO defines the data access contract for operations on the 'notifications' table.
 */
public interface NotificationDAO {

    /**
     * Inserts a new notification into the database.
     *
     * @param notification Notification entity to insert
     * @return true if insertion was successful, false otherwise
     */
    boolean addNotification(Notification notification);

    /**
     * Retrieves a notification by its unique ID.
     *
     * @param notificationId ID of the notification
     * @return Notification entity if found, null otherwise
     */
    Notification getNotificationById(int notificationId);

    /**
     * Retrieves all notifications for a specific user, ordered latest first.
     *
     * @param userId recipient user ID
     * @return list of Notification entities
     */
    List<Notification> getNotificationsByUser(int userId);

    /**
     * Retrieves all unread notifications for a specific user.
     *
     * @param userId recipient user ID
     * @return list of unread Notification entities
     */
    List<Notification> getUnreadByUser(int userId);

    /**
     * Marks a single notification as read.
     *
     * @param notificationId ID of the notification
     * @return true if updated, false otherwise
     */
    boolean markAsRead(int notificationId);

    /**
     * Marks all unread notifications of a user as read.
     *
     * @param userId recipient user ID
     * @return true if at least one notification was marked read, false otherwise
     */
    boolean markAllAsRead(int userId);

    /**
     * Deletes a notification by its ID.
     *
     * @param notificationId ID of the notification
     * @return true if deleted, false otherwise
     */
    boolean deleteNotification(int notificationId);

    /**
     * Counts the number of unread notifications for a user.
     *
     * @param userId recipient user ID
     * @return count of unread notifications
     */
    int countUnread(int userId);

    /**
     * Verifies if a user exists in the database.
     *
     * @param userId ID of the user to check
     * @return true if user exists, false otherwise
     */
    boolean userExists(int userId);
}
