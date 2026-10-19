package com.smartcampus.parcel.user.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.user.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDAO.
 * Interacts with MySQL database using PreparedStatements and try-with-resources.
 */
public class UserDAOImpl implements UserDAO {

    /**
     * Inserts a new user record into the users table.
     */
    @Override
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role, department, hostel_block) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
            pstmt.setString(4, user.getPasswordHash());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getDepartment());
            pstmt.setString(7, user.getHostelBlock());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error adding user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a user by user_id.
     */
    @Override
    public User getUserById(int userId) {
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role, department, hostel_block, created_at "
                   + "FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error finding user by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a user by unique email.
     */
    @Override
    public User getUserByEmail(String email) {
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role, department, hostel_block, created_at "
                   + "FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error finding user by email: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all users ordered by user_id.
     */
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role, department, hostel_block, created_at "
                   + "FROM users ORDER BY user_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching all users: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all users having a given role.
     */
    @Override
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role, department, hostel_block, created_at "
                   + "FROM users WHERE role = ? ORDER BY user_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error fetching users by role: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing user's details. If passwordHash is provided, updates the password as well.
     */
    @Override
    public boolean updateUser(User user) {
        boolean updatePassword = user.getPasswordHash() != null && !user.getPasswordHash().trim().isEmpty();
        String sql;
        if (updatePassword) {
            sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, password_hash = ?, role = ?, department = ?, hostel_block = ? "
                + "WHERE user_id = ?";
        } else {
            sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, role = ?, department = ?, hostel_block = ? "
                + "WHERE user_id = ?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int idx = 1;
            pstmt.setString(idx++, user.getFullName());
            pstmt.setString(idx++, user.getEmail());
            pstmt.setString(idx++, user.getPhone());
            if (updatePassword) {
                pstmt.setString(idx++, user.getPasswordHash());
            }
            pstmt.setString(idx++, user.getRole());
            pstmt.setString(idx++, user.getDepartment());
            pstmt.setString(idx++, user.getHostelBlock());
            pstmt.setInt(idx, user.getUserId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error updating user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a user record by user_id.
     */
    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error deleting user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if an email already exists in the users table.
     */
    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error checking email existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts active parcels for a recipient (status is NOT 'COLLECTED' and NOT 'CANCELLED').
     */
    @Override
    public int countActiveParcelsForRecipient(int userId) {
        String sql = "SELECT COUNT(*) FROM parcels WHERE recipient_id = ? AND status NOT IN ('COLLECTED', 'CANCELLED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error counting active parcels: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Counts active deliveries for an agent (status is 'ASSIGNED' or 'OUT_FOR_DELIVERY').
     */
    @Override
    public int countActiveDeliveriesForAgent(int agentId) {
        String sql = "SELECT COUNT(*) FROM deliveries WHERE agent_id = ? AND status IN ('ASSIGNED', 'OUT_FOR_DELIVERY')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, agentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] Error counting active deliveries: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper method to map a single ResultSet row to a User entity.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password_hash"),
                rs.getString("role"),
                rs.getString("department"),
                rs.getString("hostel_block"),
                rs.getTimestamp("created_at")
        );
    }
}
