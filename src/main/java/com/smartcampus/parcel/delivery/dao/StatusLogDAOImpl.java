package com.smartcampus.parcel.delivery.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.delivery.model.StatusLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of StatusLogDAO for the 'parcel_status_log' table.
 * Uses PreparedStatements and try-with-resources throughout.
 */
public class StatusLogDAOImpl implements StatusLogDAO {

    /**
     * Appends an audit row to parcel_status_log.
     */
    @Override
    public boolean addLog(int parcelId, String status, Integer changedByUserId, String remarks) {
        String sql = "INSERT INTO parcel_status_log (parcel_id, status, changed_by, remarks) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parcelId);
            pstmt.setString(2, status);
            if (changedByUserId != null && changedByUserId > 0) {
                pstmt.setInt(3, changedByUserId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, remarks);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[StatusLogDAO] Error inserting status log: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all status history log records for a given parcel, ordered oldest first.
     */
    @Override
    public List<StatusLog> getLogsByParcel(int parcelId) {
        List<StatusLog> list = new ArrayList<>();
        String sql = "SELECT log_id, parcel_id, status, changed_by, changed_at, remarks " +
                     "FROM parcel_status_log WHERE parcel_id = ? ORDER BY changed_at ASC, log_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, parcelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToStatusLog(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[StatusLogDAO] Error fetching logs for parcel: " + e.getMessage());
        }
        return list;
    }

    /**
     * Helper method to map a ResultSet row to a StatusLog entity.
     */
    private StatusLog mapRowToStatusLog(ResultSet rs) throws SQLException {
        int logId = rs.getInt("log_id");
        int parcelId = rs.getInt("parcel_id");
        String status = rs.getString("status");
        int changedByVal = rs.getInt("changed_by");
        Integer changedBy = rs.wasNull() ? null : changedByVal;
        java.sql.Timestamp changedAt = rs.getTimestamp("changed_at");
        String remarks = rs.getString("remarks");

        return new StatusLog(logId, parcelId, status, changedBy, changedAt, remarks);
    }
}
