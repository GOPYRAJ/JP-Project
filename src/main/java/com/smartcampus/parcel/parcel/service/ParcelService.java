package com.smartcampus.parcel.parcel.service;

import com.smartcampus.parcel.parcel.dao.ParcelDAO;
import com.smartcampus.parcel.parcel.model.Parcel;
import com.smartcampus.parcel.pickup.service.NotificationService;
import com.smartcampus.parcel.util.Constants;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * ParcelService handles business operations on parcels.
 * All validation and rules live here; SQL stays in ParcelDAO.
 * Other modules (Module 3, Module 4) call this class through its public API.
 */
public class ParcelService {

    private final ParcelDAO parcelDAO;
    private final NotificationService notificationService;

    /** Valid status values accepted by the parcels.status ENUM in schema.sql. */
    private static final Set<String> VALID_STATUSES = Set.of(
        Constants.ParcelStatus.RECEIVED,
        Constants.ParcelStatus.ASSIGNED,
        Constants.ParcelStatus.OUT_FOR_DELIVERY,
        Constants.ParcelStatus.DELIVERED,
        Constants.ParcelStatus.COLLECTED,
        Constants.ParcelStatus.FAILED,
        Constants.ParcelStatus.CANCELLED
    );

    /**
     * Statuses from which Module 2 may directly update a parcel.
     * Transitions that belong to Module 3 (ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, FAILED)
     * are handled by updateParcelStatus() which any module may call.
     */
    private static final Set<String> UI_UPDATABLE_STATUSES = Set.of(
        Constants.ParcelStatus.RECEIVED,
        Constants.ParcelStatus.CANCELLED
    );

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public ParcelService() {
        this.parcelDAO           = new ParcelDAO();
        this.notificationService = new NotificationService();
    }

    /** Constructor for testing / dependency injection. */
    public ParcelService(ParcelDAO parcelDAO, NotificationService notificationService) {
        this.parcelDAO           = parcelDAO;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------
    // Create
    // -------------------------------------------------------

    /**
     * Validates then registers a new parcel.
     * Inserts the parcel row and writes the initial RECEIVED log entry
     * in a single transaction (both handled inside ParcelDAO.addParcel()).
     * Sends a notification to the recipient on success.
     *
     * @param trackingNo     unique tracking number from the courier
     * @param senderName     name of the sender
     * @param courierCompany courier company name
     * @param recipientId    must exist in the users table
     * @param pickupPointId  must exist in the pickup_points table
     * @param weightKg       weight in kg (null is allowed)
     * @param description    optional parcel description
     * @param registeredBy   user_id of the admin registering the parcel
     * @return the new parcel_id
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException wrapping SQLException on database error
     */
    public int registerParcel(String trackingNo, String senderName, String courierCompany,
                               int recipientId, int pickupPointId, BigDecimal weightKg,
                               String description, int registeredBy) {
        // --- Validation ---
        if (isBlank(trackingNo))     throw new IllegalArgumentException("Tracking number is required.");
        if (isBlank(senderName))     throw new IllegalArgumentException("Sender name is required.");
        if (isBlank(courierCompany)) throw new IllegalArgumentException("Courier company is required.");
        if (recipientId <= 0)        throw new IllegalArgumentException("Invalid recipient ID.");
        if (pickupPointId <= 0)      throw new IllegalArgumentException("Invalid pickup point ID.");
        if (weightKg != null && weightKg.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Weight cannot be negative.");

        try {
            // Duplicate tracking number check
            if (parcelDAO.trackingNoExists(trackingNo)) {
                throw new IllegalArgumentException(
                    "Tracking number '" + trackingNo + "' already exists in the system.");
            }
            // Verify recipient exists
            if (!parcelDAO.userExists(recipientId)) {
                throw new IllegalArgumentException(
                    "Recipient user ID " + recipientId + " does not exist.");
            }
            // Verify pickup point exists
            if (!parcelDAO.pickupPointExists(pickupPointId)) {
                throw new IllegalArgumentException(
                    "Pickup point ID " + pickupPointId + " does not exist.");
            }

            Parcel parcel = new Parcel(trackingNo, senderName, courierCompany,
                recipientId, pickupPointId, weightKg, description,
                Constants.ParcelStatus.RECEIVED);

            int newId = parcelDAO.addParcel(parcel, registeredBy);

            // Notify the recipient (NotificationService.send() is safe to call even
            // when Module 4 is still a stub, as it just prints to console)
            notificationService.send(recipientId, newId,
                "Your parcel " + trackingNo + " from " + senderName +
                " has arrived on campus.");

            return newId;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while registering parcel: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Read
    // -------------------------------------------------------

    /**
     * Returns the Parcel with the given ID, or null if not found.
     *
     * @param parcelId the parcel_id to look up
     * @return Parcel or null
     */
    public Parcel getParcelById(int parcelId) {
        if (parcelId <= 0) return null;
        try {
            return parcelDAO.getParcelById(parcelId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching parcel: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all parcels ordered by most recently received.
     *
     * @return list of parcels (may be empty, never null)
     */
    public List<Parcel> getAllParcels() {
        try {
            return parcelDAO.getAllParcels();
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching parcels: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a parcel by its tracking number.
     *
     * @param trackingNo the tracking number to search
     * @return Parcel or null if not found
     */
    public Parcel searchByTrackingNo(String trackingNo) {
        if (isBlank(trackingNo)) return null;
        try {
            return parcelDAO.searchByTrackingNo(trackingNo.trim());
        } catch (SQLException e) {
            throw new RuntimeException("Database error while searching by tracking number: " + e.getMessage(), e);
        }
    }

    /**
     * Returns all parcels belonging to a specific recipient.
     *
     * @param recipientId the user_id of the recipient
     * @return list of parcels (may be empty, never null)
     */
    public List<Parcel> getByRecipient(int recipientId) {
        if (recipientId <= 0) return List.of();
        try {
            return parcelDAO.getByRecipient(recipientId);
        } catch (SQLException e) {
            throw new RuntimeException("Database error while fetching parcels by recipient: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Update — parcel details
    // -------------------------------------------------------

    /**
     * Updates the non-status fields of an existing parcel.
     * Re-validates recipient and pickup point existence on update.
     *
     * @param parcel parcel with updated fields; parcelId must be set and > 0
     * @return true if the update succeeded
     * @throws IllegalArgumentException if validation fails
     */
    public boolean updateParcel(Parcel parcel) {
        if (parcel == null || parcel.getParcelId() <= 0)
            throw new IllegalArgumentException("Invalid parcel ID.");
        if (isBlank(parcel.getTrackingNo()))     throw new IllegalArgumentException("Tracking number is required.");
        if (isBlank(parcel.getSenderName()))     throw new IllegalArgumentException("Sender name is required.");
        if (isBlank(parcel.getCourierCompany())) throw new IllegalArgumentException("Courier company is required.");
        if (parcel.getRecipientId() <= 0)        throw new IllegalArgumentException("Invalid recipient ID.");
        if (parcel.getPickupPointId() <= 0)      throw new IllegalArgumentException("Invalid pickup point ID.");

        try {
            // Verify the parcel exists
            if (parcelDAO.getParcelById(parcel.getParcelId()) == null) {
                throw new IllegalArgumentException("Parcel ID " + parcel.getParcelId() + " not found.");
            }
            if (!parcelDAO.userExists(parcel.getRecipientId())) {
                throw new IllegalArgumentException(
                    "Recipient user ID " + parcel.getRecipientId() + " does not exist.");
            }
            if (!parcelDAO.pickupPointExists(parcel.getPickupPointId())) {
                throw new IllegalArgumentException(
                    "Pickup point ID " + parcel.getPickupPointId() + " does not exist.");
            }
            return parcelDAO.updateParcel(parcel);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating parcel: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Update — status  (public API for Module 3 and other modules)
    // -------------------------------------------------------

    /**
     * Updates the status of a parcel and appends a parcel_status_log row.
     * This is the shared API for Module 3 (Delivery & Tracking) and
     * other modules to call when a parcel changes state.
     *
     * @param parcelId id of the parcel to update
     * @param status   new status — must be one of Constants.ParcelStatus values
     * @throws IllegalArgumentException if the parcel is not found or status is invalid
     * @throws RuntimeException wrapping SQLException on database error
     */
    public void updateParcelStatus(int parcelId, String status) {
        updateParcelStatus(parcelId, status, 0, null);
    }

    /**
     * Full variant of updateParcelStatus with caller identity and remarks,
     * for use within the Module 2 UI and by other modules that can supply context.
     *
     * @param parcelId  id of the parcel to update
     * @param status    new status — must be one of Constants.ParcelStatus values
     * @param changedBy user_id of the user making the change (0 = system/unknown)
     * @param remarks   optional note written into parcel_status_log
     * @throws IllegalArgumentException if the parcel is not found or status is invalid
     * @throws RuntimeException wrapping SQLException on database error
     */
    public void updateParcelStatus(int parcelId, String status, int changedBy, String remarks) {
        if (parcelId <= 0) throw new IllegalArgumentException("Invalid parcel ID.");
        if (isBlank(status) || !VALID_STATUSES.contains(status.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid status '" + status + "'. Allowed: " + VALID_STATUSES);
        }
        String normalised = status.toUpperCase();

        try {
            Parcel existing = parcelDAO.getParcelById(parcelId);
            if (existing == null) {
                throw new IllegalArgumentException("Parcel ID " + parcelId + " not found.");
            }
            boolean updated = parcelDAO.updateParcelStatus(parcelId, normalised, changedBy, remarks);
            if (!updated) {
                throw new RuntimeException("Status update failed for parcel ID " + parcelId);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating parcel status: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Delete
    // -------------------------------------------------------

    /**
     * Deletes a parcel only when its current status is RECEIVED or CANCELLED.
     * This enforces the business rule stated in the README.
     *
     * @param parcelId the parcel to delete
     * @return true if deleted successfully
     * @throws IllegalArgumentException if the parcel is not found or deletion is not permitted
     */
    public boolean deleteParcel(int parcelId) {
        if (parcelId <= 0) throw new IllegalArgumentException("Invalid parcel ID.");
        try {
            Parcel parcel = parcelDAO.getParcelById(parcelId);
            if (parcel == null) {
                throw new IllegalArgumentException("Parcel ID " + parcelId + " not found.");
            }
            String currentStatus = parcel.getStatus();
            if (!Constants.ParcelStatus.RECEIVED.equals(currentStatus) &&
                !Constants.ParcelStatus.CANCELLED.equals(currentStatus)) {
                throw new IllegalArgumentException(
                    "Cannot delete parcel with status '" + currentStatus +
                    "'. Only RECEIVED or CANCELLED parcels may be deleted.");
            }
            return parcelDAO.deleteParcel(parcelId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting parcel: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    /** Returns true if the string is null or blank. */
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
