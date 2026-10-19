package com.smartcampus.parcel.pickup.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.pickup.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of NotificationDAO.
 * Interacts with MySQL database using PreparedStatements and try-with-resources.
 */
public class NotificationDAOImpl implements NotificationDAO {

    /**
     * Inserts a new notification into the notifications table.
     */
    @Override
    public boolean addNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, parcel_id, message, is_read) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, notification.getUserId());
            if (notification.getParcelId() != null && notification.getParcelId() > 0) {
                pstmt.setInt(2, notification.getParcelId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, notification.getMessage());
            pstmt.setBoolean(4, notification.isRead());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notification.setNotificationId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error adding notification: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a notification by notification_id.
     */
    @Override
    public Notification getNotificationById(int notificationId) {
        String sql = "SELECT notification_id, user_id, parcel_id, message, is_read, created_at "
                   + "FROM notifications WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNotification(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error finding notification by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all notifications for a specific user, ordered latest first.
     */
    @Override
    public List<Notification> getNotificationsByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, parcel_id, message, is_read, created_at "
                   + "FROM notifications WHERE user_id = ? ORDER BY created_at DESC, notification_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error fetching notifications for user: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all unread notifications for a specific user, ordered latest first.
     */
    @Override
    public List<Notification> getUnreadByUser(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT notification_id, user_id, parcel_id, message, is_read, created_at "
                   + "FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC, notification_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToNotification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error fetching unread notifications for user: " + e.getMessage());
        }
        return list;
    }

    /**
     * Marks a single notification as read.
     */
    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking notification as read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Marks all unread notifications of a user as read.
     */
    @Override
    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error marking all notifications read: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a notification by its primary key ID.
     */
    @Override
    public boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notificationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error deleting notification: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts unread notifications for a specific user.
     */
    @Override
    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error counting unread notifications: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if a user exists in the users table.
     */
    @Override
    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] Error verifying user existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Helper method to map a single ResultSet row to a Notification entity.
     */
    private Notification mapRowToNotification(ResultSet rs) throws SQLException {
        int rawParcelId = rs.getInt("parcel_id");
        Integer parcelId = rs.wasNull() ? null : rawParcelId;

        return new Notification(
                rs.getInt("notification_id"),
                rs.getInt("user_id"),
                parcelId,
                rs.getString("message"),
                rs.getBoolean("is_read"),
                rs.getTimestamp("created_at")
        );
    }
}
