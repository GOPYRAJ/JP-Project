package com.smartcampus.parcel.parcel.dao;

import com.smartcampus.parcel.config.DBConnection;
import com.smartcampus.parcel.parcel.model.Parcel;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ParcelDAO performs all SQL / JDBC operations for the parcels table
 * and the parcel_status_log table (initial log entry on creation).
 * Uses PreparedStatement and try-with-resources throughout.
 * No business logic or console I/O belongs here.
 */
public class ParcelDAO {

    // -------------------------------------------------------
    // SQL constants — column names match schema.sql exactly
    // -------------------------------------------------------

    private static final String SQL_INSERT_PARCEL =
        "INSERT INTO parcels (tracking_no, sender_name, courier_company, recipient_id, " +
        "pickup_point_id, weight_kg, description, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_STATUS_LOG =
        "INSERT INTO parcel_status_log (parcel_id, status, changed_by, remarks) " +
        "VALUES (?, ?, ?, ?)";

    private static final String SQL_SELECT_BY_ID =
        "SELECT parcel_id, tracking_no, sender_name, courier_company, recipient_id, " +
        "pickup_point_id, weight_kg, description, status, received_at, collected_at " +
        "FROM parcels WHERE parcel_id = ?";

    private static final String SQL_SELECT_ALL =
        "SELECT parcel_id, tracking_no, sender_name, courier_company, recipient_id, " +
        "pickup_point_id, weight_kg, description, status, received_at, collected_at " +
        "FROM parcels ORDER BY received_at DESC";

    private static final String SQL_SELECT_BY_TRACKING =
        "SELECT parcel_id, tracking_no, sender_name, courier_company, recipient_id, " +
        "pickup_point_id, weight_kg, description, status, received_at, collected_at " +
        "FROM parcels WHERE tracking_no = ?";

    private static final String SQL_SELECT_BY_RECIPIENT =
        "SELECT parcel_id, tracking_no, sender_name, courier_company, recipient_id, " +
        "pickup_point_id, weight_kg, description, status, received_at, collected_at " +
        "FROM parcels WHERE recipient_id = ? ORDER BY received_at DESC";

    private static final String SQL_UPDATE_PARCEL =
        "UPDATE parcels SET tracking_no = ?, sender_name = ?, courier_company = ?, " +
        "recipient_id = ?, pickup_point_id = ?, weight_kg = ?, description = ? " +
        "WHERE parcel_id = ?";

    private static final String SQL_UPDATE_STATUS =
        "UPDATE parcels SET status = ? WHERE parcel_id = ?";

    private static final String SQL_UPDATE_COLLECTED_AT =
        "UPDATE parcels SET collected_at = CURRENT_TIMESTAMP WHERE parcel_id = ?";

    private static final String SQL_DELETE_PARCEL =
        "DELETE FROM parcels WHERE parcel_id = ?";

    private static final String SQL_CHECK_TRACKING_EXISTS =
        "SELECT COUNT(*) FROM parcels WHERE tracking_no = ?";

    private static final String SQL_CHECK_USER_EXISTS =
        "SELECT COUNT(*) FROM users WHERE user_id = ?";

    private static final String SQL_CHECK_PICKUP_POINT_EXISTS =
        "SELECT COUNT(*) FROM pickup_points WHERE point_id = ?";

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

    /**
     * Inserts a new parcel row and immediately logs the initial RECEIVED status
     * in parcel_status_log, both within the same transaction.
     *
     * @param parcel     the parcel to persist (parcelId will be set on return)
     * @param changedBy  user_id of the admin registering the parcel (used in log)
     * @return the generated parcel_id, or -1 on failure
     * @throws SQLException if a database error occurs
     */
    public int addParcel(Parcel parcel, int changedBy) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int generatedId = insertParcel(conn, parcel);
                insertStatusLog(conn, generatedId, parcel.getStatus(), changedBy,
                    "Parcel registered at intake");
                conn.commit();
                parcel.setParcelId(generatedId);
                return generatedId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Executes the INSERT into parcels and returns the generated key. */
    private int insertParcel(Connection conn, Parcel parcel) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_PARCEL,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, parcel.getTrackingNo());
            ps.setString(2, parcel.getSenderName());
            ps.setString(3, parcel.getCourierCompany());
            ps.setInt(4, parcel.getRecipientId());
            ps.setInt(5, parcel.getPickupPointId());
            if (parcel.getWeightKg() != null) {
                ps.setBigDecimal(6, parcel.getWeightKg());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setString(7, parcel.getDescription());
            ps.setString(8, parcel.getStatus());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new SQLException("No generated key returned for parcel insert.");
            }
        }
    }

    /**
     * Inserts a row into parcel_status_log.
     * Called both during initial registration and during status updates.
     *
     * @param conn      active connection (may be part of a transaction)
     * @param parcelId  the parcel being logged
     * @param status    the new status string
     * @param changedBy user_id of the person making the change (0 = system)
     * @param remarks   optional human-readable note
     * @throws SQLException if a database error occurs
     */
    public void insertStatusLog(Connection conn, int parcelId, String status,
                                 int changedBy, String remarks) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT_STATUS_LOG)) {
            ps.setInt(1, parcelId);
            ps.setString(2, status);
            if (changedBy > 0) {
                ps.setInt(3, changedBy);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, remarks);
            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Read
    // -------------------------------------------------------

    /**
     * Retrieves a parcel by its primary key.
     *
     * @param parcelId the parcel_id to look up
     * @return Parcel object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Parcel getParcelById(int parcelId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            ps.setInt(1, parcelId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all parcels, ordered by most recently received.
     *
     * @return list of all Parcel objects (may be empty, never null)
     * @throws SQLException if a database error occurs
     */
    public List<Parcel> getAllParcels() throws SQLException {
        List<Parcel> parcels = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                parcels.add(mapRow(rs));
            }
        }
        return parcels;
    }

    /**
     * Finds a parcel by its unique tracking number.
     *
     * @param trackingNo the tracking number to search for
     * @return Parcel object, or null if not found
     * @throws SQLException if a database error occurs
     */
    public Parcel searchByTrackingNo(String trackingNo) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_TRACKING)) {
            ps.setString(1, trackingNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Retrieves all parcels belonging to a specific recipient.
     *
     * @param recipientId the user_id of the recipient
     * @return list of parcels for that recipient (may be empty, never null)
     * @throws SQLException if a database error occurs
     */
    public List<Parcel> getByRecipient(int recipientId) throws SQLException {
        List<Parcel> parcels = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_SELECT_BY_RECIPIENT)) {
            ps.setInt(1, recipientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parcels.add(mapRow(rs));
                }
            }
        }
        return parcels;
    }

    // -------------------------------------------------------
    // Update
    // -------------------------------------------------------

    /**
     * Updates the editable fields of an existing parcel.
     * The status and timestamps are NOT changed here; use updateParcelStatus() for that.
     *
     * @param parcel parcel with updated fields; parcelId must be set
     * @return true if exactly one row was updated
     * @throws SQLException if a database error occurs
     */
    public boolean updateParcel(Parcel parcel) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_PARCEL)) {
            ps.setString(1, parcel.getTrackingNo());
            ps.setString(2, parcel.getSenderName());
            ps.setString(3, parcel.getCourierCompany());
            ps.setInt(4, parcel.getRecipientId());
            ps.setInt(5, parcel.getPickupPointId());
            if (parcel.getWeightKg() != null) {
                ps.setBigDecimal(6, parcel.getWeightKg());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setString(7, parcel.getDescription());
            ps.setInt(8, parcel.getParcelId());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Updates only the status column of the given parcel, and appends a row to
     * parcel_status_log, within the same transaction.
     * If the new status is COLLECTED, collected_at is also stamped.
     *
     * @param parcelId  parcel to update
     * @param status    new status string (use Constants.ParcelStatus values)
     * @param changedBy user_id of the person making the change
     * @param remarks   optional note for the log entry
     * @return true if the parcel row was updated
     * @throws SQLException if a database error occurs
     */
    public boolean updateParcelStatus(int parcelId, String status,
                                       int changedBy, String remarks) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update the parcels.status column
                try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUS)) {
                    ps.setString(1, status);
                    ps.setInt(2, parcelId);
                    int rows = ps.executeUpdate();
                    if (rows != 1) {
                        conn.rollback();
                        return false;
                    }
                }
                // Stamp collected_at when the parcel is collected
                if ("COLLECTED".equals(status)) {
                    try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_COLLECTED_AT)) {
                        ps.setInt(1, parcelId);
                        ps.executeUpdate();
                    }
                }
                // Append to status history
                insertStatusLog(conn, parcelId, status, changedBy, remarks);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // -------------------------------------------------------
    // Delete
    // -------------------------------------------------------

    /**
     * Permanently deletes a parcel row.
     * The service layer is responsible for checking status before calling this.
     *
     * @param parcelId the parcel to delete
     * @return true if exactly one row was deleted
     * @throws SQLException if a database error occurs
     */
    public boolean deleteParcel(int parcelId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_PARCEL)) {
            ps.setInt(1, parcelId);
            return ps.executeUpdate() == 1;
        }
    }

    // -------------------------------------------------------
    // Helper / existence checks
    // -------------------------------------------------------

    /**
     * Checks whether a tracking number is already in use.
     *
     * @param trackingNo the tracking number to check
     * @return true if another parcel already has this tracking number
     * @throws SQLException if a database error occurs
     */
    public boolean trackingNoExists(String trackingNo) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CHECK_TRACKING_EXISTS)) {
            ps.setString(1, trackingNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks whether a user with the given ID exists in the users table.
     *
     * @param userId the user_id to verify
     * @return true if the user exists
     * @throws SQLException if a database error occurs
     */
    public boolean userExists(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CHECK_USER_EXISTS)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Checks whether a pickup point with the given ID exists in pickup_points.
     *
     * @param pointId the point_id to verify
     * @return true if the pickup point exists
     * @throws SQLException if a database error occurs
     */
    public boolean pickupPointExists(int pointId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_CHECK_PICKUP_POINT_EXISTS)) {
            ps.setInt(1, pointId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // -------------------------------------------------------
    // Private mapping helper
    // -------------------------------------------------------

    /**
     * Maps a single ResultSet row to a Parcel object.
     * Column names match schema.sql exactly.
     */
    private Parcel mapRow(ResultSet rs) throws SQLException {
        Parcel p = new Parcel();
        p.setParcelId(rs.getInt("parcel_id"));
        p.setTrackingNo(rs.getString("tracking_no"));
        p.setSenderName(rs.getString("sender_name"));
        p.setCourierCompany(rs.getString("courier_company"));
        p.setRecipientId(rs.getInt("recipient_id"));
        p.setPickupPointId(rs.getInt("pickup_point_id"));
        p.setWeightKg(rs.getBigDecimal("weight_kg"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        p.setReceivedAt(rs.getTimestamp("received_at"));
        p.setCollectedAt(rs.getTimestamp("collected_at"));
        return p;
    }
}
