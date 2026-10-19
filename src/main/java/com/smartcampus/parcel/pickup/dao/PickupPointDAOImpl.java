package com.smartcampus.parcel.pickup.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.pickup.model.PickupPoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of PickupPointDAO.
 * Interacts with MySQL database using PreparedStatements and try-with-resources.
 */
public class PickupPointDAOImpl implements PickupPointDAO {

    /**
     * Inserts a new pickup point record into the pickup_points table.
     */
    @Override
    public boolean addPickupPoint(PickupPoint point) {
        String sql = "INSERT INTO pickup_points (name, location, capacity, is_active) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, point.getName());
            pstmt.setString(2, point.getLocation());
            pstmt.setInt(3, point.getCapacity());
            pstmt.setBoolean(4, point.isActive());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        point.setPointId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error adding pickup point: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a pickup point by its point_id.
     */
    @Override
    public PickupPoint getPickupPointById(int pointId) {
        String sql = "SELECT point_id, name, location, capacity, is_active FROM pickup_points WHERE point_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pointId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPickupPoint(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error fetching pickup point by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a pickup point by unique name.
     */
    @Override
    public PickupPoint getPickupPointByName(String name) {
        String sql = "SELECT point_id, name, location, capacity, is_active FROM pickup_points WHERE name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPickupPoint(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error fetching pickup point by name: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all pickup points ordered by point_id.
     */
    @Override
    public List<PickupPoint> getAllPickupPoints() {
        List<PickupPoint> list = new ArrayList<>();
        String sql = "SELECT point_id, name, location, capacity, is_active FROM pickup_points ORDER BY point_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToPickupPoint(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error fetching all pickup points: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all active pickup points ordered by point_id.
     */
    @Override
    public List<PickupPoint> getActivePickupPoints() {
        List<PickupPoint> list = new ArrayList<>();
        String sql = "SELECT point_id, name, location, capacity, is_active FROM pickup_points WHERE is_active = TRUE ORDER BY point_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToPickupPoint(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error fetching active pickup points: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing pickup point's details.
     */
    @Override
    public boolean updatePickupPoint(PickupPoint point) {
        String sql = "UPDATE pickup_points SET name = ?, location = ?, capacity = ?, is_active = ? WHERE point_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, point.getName());
            pstmt.setString(2, point.getLocation());
            pstmt.setInt(3, point.getCapacity());
            pstmt.setBoolean(4, point.isActive());
            pstmt.setInt(5, point.getPointId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error updating pickup point: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a pickup point record by point_id.
     */
    @Override
    public boolean deletePickupPoint(int pointId) {
        String sql = "DELETE FROM pickup_points WHERE point_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pointId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error deleting pickup point: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates the active status of a pickup point.
     */
    @Override
    public boolean setActive(int pointId, boolean active) {
        String sql = "UPDATE pickup_points SET is_active = ? WHERE point_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, active);
            pstmt.setInt(2, pointId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error setting active status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts active parcels at the pickup point (status is not COLLECTED or CANCELLED).
     */
    @Override
    public int countActiveParcelsAtPoint(int pointId) {
        String sql = "SELECT COUNT(*) FROM parcels WHERE pickup_point_id = ? AND status NOT IN ('COLLECTED', 'CANCELLED')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, pointId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PickupPointDAO] Error counting active parcels at pickup point: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper method to map a single ResultSet row to a PickupPoint entity.
     */
    private PickupPoint mapRowToPickupPoint(ResultSet rs) throws SQLException {
        return new PickupPoint(
                rs.getInt("point_id"),
                rs.getString("name"),
                rs.getString("location"),
                rs.getInt("capacity"),
                rs.getBoolean("is_active")
        );
    }
}
