package com.smartcampus.parcel.delivery.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.delivery.model.Delivery;
import com.smartcampus.parcel.util.Constants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of DeliveryDAO for the 'deliveries' table.
 * Uses PreparedStatements and try-with-resources throughout.
 */
public class DeliveryDAOImpl implements DeliveryDAO {

    /**
     * Inserts a new delivery record and assigns the generated primary key to the delivery object.
     */
    @Override
    public boolean addDelivery(Delivery delivery) {
        String sql = "INSERT INTO deliveries (parcel_id, agent_id, status, remarks) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, delivery.getParcelId());
            pstmt.setInt(2, delivery.getAgentId());
            pstmt.setString(3, delivery.getStatus());
            pstmt.setString(4, delivery.getRemarks());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        delivery.setDeliveryId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error adding delivery: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves a delivery by its primary key delivery_id.
     */
    @Override
    public Delivery getDeliveryById(int deliveryId) {
        String sql = "SELECT delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks " +
                     "FROM deliveries WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deliveryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDelivery(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error fetching delivery by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the latest delivery record associated with a given parcel.
     */
    @Override
    public Delivery getDeliveryByParcelId(int parcelId) {
        String sql = "SELECT delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks " +
                     "FROM deliveries WHERE parcel_id = ? ORDER BY delivery_id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parcelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDelivery(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error fetching delivery by parcel ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves all deliveries assigned to a specific delivery agent.
     */
    @Override
    public List<Delivery> getDeliveriesByAgent(int agentId) {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks " +
                     "FROM deliveries WHERE agent_id = ? ORDER BY delivery_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, agentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDelivery(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error fetching deliveries by agent: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all deliveries matching a given status.
     */
    @Override
    public List<Delivery> getDeliveriesByStatus(String status) {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks " +
                     "FROM deliveries WHERE status = ? ORDER BY delivery_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDelivery(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error fetching deliveries by status: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves all deliveries in the database ordered newest first.
     */
    @Override
    public List<Delivery> getAllDeliveries() {
        List<Delivery> list = new ArrayList<>();
        String sql = "SELECT delivery_id, parcel_id, agent_id, status, assigned_at, delivered_at, remarks " +
                     "FROM deliveries ORDER BY delivery_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToDelivery(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error fetching all deliveries: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates the status of a delivery. If the status is DELIVERED, sets delivered_at to CURRENT_TIMESTAMP.
     */
    @Override
    public boolean updateDeliveryStatus(int deliveryId, String status) {
        boolean isDelivered = Constants.DeliveryStatus.DELIVERED.equalsIgnoreCase(status);
        String sql = isDelivered
                ? "UPDATE deliveries SET status = ?, delivered_at = CURRENT_TIMESTAMP WHERE delivery_id = ?"
                : "UPDATE deliveries SET status = ? WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, deliveryId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error updating delivery status: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates remarks for a delivery record.
     */
    @Override
    public boolean updateRemarks(int deliveryId, String remarks) {
        String sql = "UPDATE deliveries SET remarks = ? WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, remarks);
            pstmt.setInt(2, deliveryId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error updating delivery remarks: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a delivery record by primary key ID.
     */
    @Override
    public boolean deleteDelivery(int deliveryId) {
        String sql = "DELETE FROM deliveries WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, deliveryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DeliveryDAO] Error deleting delivery: " + e.getMessage());
        }
        return false;
    }

    /**
     * Counts active deliveries (ASSIGNED or OUT_FOR_DELIVERY) for a delivery agent.
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
            System.err.println("[DeliveryDAO] Error counting active deliveries for agent: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Helper method to map a ResultSet row to a Delivery entity.
     */
    private Delivery mapRowToDelivery(ResultSet rs) throws SQLException {
        return new Delivery(
                rs.getInt("delivery_id"),
                rs.getInt("parcel_id"),
                rs.getInt("agent_id"),
                rs.getString("status"),
                rs.getTimestamp("assigned_at"),
                rs.getTimestamp("delivered_at"),
                rs.getString("remarks")
        );
    }
}
