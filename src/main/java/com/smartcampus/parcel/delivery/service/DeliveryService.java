package com.smartcampus.parcel.delivery.service;

import com.smartcampus.parcel.delivery.dao.DeliveryDAO;
import com.smartcampus.parcel.delivery.dao.DeliveryDAOImpl;
import com.smartcampus.parcel.delivery.dao.StatusLogDAO;
import com.smartcampus.parcel.delivery.dao.StatusLogDAOImpl;
import com.smartcampus.parcel.delivery.model.Delivery;
import com.smartcampus.parcel.delivery.model.StatusLog;
import com.smartcampus.parcel.parcel.model.Parcel;
import com.smartcampus.parcel.parcel.service.ParcelService;
import com.smartcampus.parcel.pickup.service.NotificationService;
import com.smartcampus.parcel.user.model.User;
import com.smartcampus.parcel.user.service.UserService;
import com.smartcampus.parcel.util.Constants;

import java.util.Collections;
import java.util.List;

/**
 * DeliveryService handles business operations and workflow transitions for Module 3 (Delivery & Tracking).
 * Enforces business rules and delegates data storage to DAOs and other module services.
 * Does not perform direct console I/O or throw SQL exceptions to callers.
 */
public class DeliveryService {

    private final DeliveryDAO deliveryDAO;
    private final StatusLogDAO statusLogDAO;
    private final ParcelService parcelService;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * Default constructor initializing production dependencies.
     */
    public DeliveryService() {
        this(new DeliveryDAOImpl(), new StatusLogDAOImpl(),
             new ParcelService(), new UserService(), new NotificationService());
    }

    /**
     * Constructor allowing dependency injection for testing.
     *
     * @param deliveryDAO         DeliveryDAO implementation
     * @param statusLogDAO        StatusLogDAO implementation
     * @param parcelService       ParcelService instance
     * @param userService         UserService instance
     * @param notificationService NotificationService instance
     */
    public DeliveryService(DeliveryDAO deliveryDAO, StatusLogDAO statusLogDAO,
                           ParcelService parcelService, UserService userService,
                           NotificationService notificationService) {
        this.deliveryDAO = deliveryDAO;
        this.statusLogDAO = statusLogDAO;
        this.parcelService = parcelService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Assigns a parcel to a delivery agent.
     * Rules:
     * - Parcel must exist.
     * - Parcel status must be RECEIVED (or FAILED for re-assignment).
     * - Parcel must not have an active delivery (status ASSIGNED or OUT_FOR_DELIVERY).
     * - Agent must exist and have the DELIVERY_AGENT role.
     * Execution order:
     * 1. Create delivery row with status ASSIGNED.
     * 2. Set parcel status to ASSIGNED via ParcelService.
     * 3. Add parcel status log row.
     * 4. Dispatch notification to recipient.
     *
     * @param parcelId ID of the parcel to assign
     * @param agentId  ID of the delivery agent
     * @return clear result message suitable for UI display
     */
    public String assignDelivery(int parcelId, int agentId) {
        return assignDelivery(parcelId, agentId, agentId);
    }

    /**
     * Assigns a parcel to a delivery agent with explicit changedBy user ID for audit log.
     *
     * @param parcelId  ID of the parcel
     * @param agentId   ID of the delivery agent
     * @param changedBy ID of the user performing the assignment
     * @return clear result message suitable for UI display
     */
    public String assignDelivery(int parcelId, int agentId, Integer changedBy) {
        if (parcelId <= 0 || agentId <= 0) {
            return "ERROR: Parcel ID and Agent ID must be positive integers.";
        }

        // 1. Verify parcel existence
        Parcel parcel = parcelService.getParcelById(parcelId);
        if (parcel == null) {
            return "ERROR: Parcel #" + parcelId + " does not exist.";
        }

        // 2. Verify parcel status is RECEIVED or FAILED (reassignment)
        String parcelStatus = parcel.getStatus();
        if (!Constants.ParcelStatus.RECEIVED.equalsIgnoreCase(parcelStatus) &&
            !Constants.ParcelStatus.FAILED.equalsIgnoreCase(parcelStatus)) {
            return "ERROR: Cannot assign parcel #" + parcelId + ". Current status is '" + parcelStatus +
                   "'. Only parcels with status RECEIVED or FAILED can be assigned.";
        }

        // 3. Verify no existing active delivery
        Delivery existingDelivery = deliveryDAO.getDeliveryByParcelId(parcelId);
        if (existingDelivery != null) {
            String activeStatus = existingDelivery.getStatus();
            if (Constants.DeliveryStatus.ASSIGNED.equalsIgnoreCase(activeStatus) ||
                Constants.DeliveryStatus.OUT_FOR_DELIVERY.equalsIgnoreCase(activeStatus)) {
                return "ERROR: Parcel #" + parcelId + " already has an active delivery (Delivery #" +
                       existingDelivery.getDeliveryId() + ", status: " + activeStatus + ").";
            }
        }

        // 4. Verify agent exists and role is DELIVERY_AGENT
        User agent = userService.getUserById(agentId);
        if (agent == null) {
            return "ERROR: User #" + agentId + " does not exist.";
        }
        if (!Constants.Roles.DELIVERY_AGENT.equalsIgnoreCase(agent.getRole())) {
            return "ERROR: User #" + agentId + " (" + agent.getFullName() + ") is a " +
                   agent.getRole() + ", not a DELIVERY_AGENT.";
        }

        // Step 1: Create delivery row first
        Delivery delivery = new Delivery(parcelId, agentId, Constants.DeliveryStatus.ASSIGNED,
                "Assigned to agent " + agent.getFullName());
        boolean created = deliveryDAO.addDelivery(delivery);
        if (!created) {
            return "ERROR: Failed to save delivery assignment into database.";
        }

        // Step 2: Update parcel status through ParcelService
        try {
            parcelService.updateParcelStatus(parcelId, Constants.ParcelStatus.ASSIGNED);
        } catch (Exception e) {
            // Inconsistency handling: rollback newly created delivery record if parcel update fails
            deliveryDAO.deleteDelivery(delivery.getDeliveryId());
            return "ERROR: Failed to update parcel status: " + e.getMessage();
        }

        // Step 3: Write status log row
        statusLogDAO.addLog(parcelId, Constants.ParcelStatus.ASSIGNED, changedBy,
                "Delivery assigned to agent " + agent.getFullName() + " (ID #" + agentId + ")");

        // Step 4: Notify recipient
        notificationService.send(parcel.getRecipientId(), parcelId,
                "Your parcel " + parcel.getTrackingNo() + " has been assigned for campus delivery.");

        return "SUCCESS: Parcel #" + parcelId + " successfully assigned to agent " +
               agent.getFullName() + " (Delivery #" + delivery.getDeliveryId() + ").";
    }

    /**
     * Updates the status of an existing delivery assignment.
     * Transitions permitted:
     * - ASSIGNED -> OUT_FOR_DELIVERY
     * - OUT_FOR_DELIVERY -> DELIVERED
     * - OUT_FOR_DELIVERY -> FAILED
     * All other transitions are rejected.
     * Execution order:
     * 1. Update delivery row.
     * 2. Update parcel status via ParcelService.
     * 3. Write status log row.
     * 4. Notify recipient for OUT_FOR_DELIVERY, DELIVERED, and FAILED.
     *
     * @param deliveryId ID of the delivery record
     * @param newStatus  target status (OUT_FOR_DELIVERY, DELIVERED, or FAILED)
     * @param changedBy  user ID initiating the update
     * @param remarks    optional remarks / reason
     * @return clear result message suitable for UI display
     */
    public String updateDeliveryStatus(int deliveryId, String newStatus, Integer changedBy, String remarks) {
        if (deliveryId <= 0) {
            return "ERROR: Invalid delivery ID.";
        }
        if (newStatus == null || newStatus.trim().isEmpty()) {
            return "ERROR: New status cannot be empty.";
        }

        Delivery delivery = deliveryDAO.getDeliveryById(deliveryId);
        if (delivery == null) {
            return "ERROR: Delivery #" + deliveryId + " does not exist.";
        }

        String currentStatus = delivery.getStatus();
        String targetStatus = newStatus.trim().toUpperCase();

        // Validate state transitions
        boolean validTransition = false;
        if (Constants.DeliveryStatus.ASSIGNED.equalsIgnoreCase(currentStatus) &&
            Constants.DeliveryStatus.OUT_FOR_DELIVERY.equalsIgnoreCase(targetStatus)) {
            validTransition = true;
        } else if (Constants.DeliveryStatus.OUT_FOR_DELIVERY.equalsIgnoreCase(currentStatus) &&
            (Constants.DeliveryStatus.DELIVERED.equalsIgnoreCase(targetStatus) ||
             Constants.DeliveryStatus.FAILED.equalsIgnoreCase(targetStatus))) {
            validTransition = true;
        }

        if (!validTransition) {
            return "ERROR: Invalid status transition from '" + currentStatus + "' to '" + targetStatus +
                   "'. Allowed transitions: ASSIGNED -> OUT_FOR_DELIVERY, OUT_FOR_DELIVERY -> DELIVERED, or OUT_FOR_DELIVERY -> FAILED.";
        }

        // Step 1: Update delivery row first
        boolean updated = deliveryDAO.updateDeliveryStatus(deliveryId, targetStatus);
        if (!updated) {
            return "ERROR: Failed to update delivery #" + deliveryId + " in database.";
        }
        if (remarks != null && !remarks.trim().isEmpty()) {
            deliveryDAO.updateRemarks(deliveryId, remarks.trim());
        }

        // Step 2: Update parcel status through ParcelService
        try {
            parcelService.updateParcelStatus(delivery.getParcelId(), targetStatus);
        } catch (Exception e) {
            // Inconsistency handling: revert delivery status if parcel update fails
            deliveryDAO.updateDeliveryStatus(deliveryId, currentStatus);
            return "ERROR: Failed to update parcel status: " + e.getMessage();
        }

        // Step 3: Write status log row
        String logRemarks = (remarks != null && !remarks.trim().isEmpty())
                ? remarks.trim()
                : "Delivery status updated to " + targetStatus;
        statusLogDAO.addLog(delivery.getParcelId(), targetStatus, changedBy, logRemarks);

        // Step 4: Dispatch notification to recipient
        Parcel parcel = parcelService.getParcelById(delivery.getParcelId());
        if (parcel != null) {
            String notifMsg = switch (targetStatus) {
                case Constants.DeliveryStatus.OUT_FOR_DELIVERY ->
                    "Your parcel " + parcel.getTrackingNo() + " is out for delivery.";
                case Constants.DeliveryStatus.DELIVERED ->
                    "Your parcel " + parcel.getTrackingNo() + " has been DELIVERED.";
                case Constants.DeliveryStatus.FAILED ->
                    "Delivery attempt for parcel " + parcel.getTrackingNo() + " FAILED. Reason: " +
                    (remarks != null && !remarks.trim().isEmpty() ? remarks.trim() : "Unable to reach recipient");
                default -> null;
            };

            if (notifMsg != null) {
                notificationService.send(parcel.getRecipientId(), delivery.getParcelId(), notifMsg);
            }
        }

        return "SUCCESS: Delivery #" + deliveryId + " status updated from '" +
               currentStatus + "' to '" + targetStatus + "'.";
    }

    /**
     * Cancels an existing delivery assignment while it is in ASSIGNED status.
     * Deletes the delivery, sets parcel back to RECEIVED, and logs the change.
     *
     * @param deliveryId ID of the delivery record to cancel
     * @param changedBy  user ID performing the cancellation
     * @return clear result message suitable for UI display
     */
    public String cancelDelivery(int deliveryId, Integer changedBy) {
        if (deliveryId <= 0) {
            return "ERROR: Invalid delivery ID.";
        }

        Delivery delivery = deliveryDAO.getDeliveryById(deliveryId);
        if (delivery == null) {
            return "ERROR: Delivery #" + deliveryId + " does not exist.";
        }

        if (!Constants.DeliveryStatus.ASSIGNED.equalsIgnoreCase(delivery.getStatus())) {
            return "ERROR: Cannot cancel delivery #" + deliveryId + ". Only deliveries with status ASSIGNED " +
                   "can be cancelled (current status: " + delivery.getStatus() + ").";
        }

        int parcelId = delivery.getParcelId();

        // Step 1: Delete delivery row
        boolean deleted = deliveryDAO.deleteDelivery(deliveryId);
        if (!deleted) {
            return "ERROR: Failed to delete delivery assignment #" + deliveryId + " from database.";
        }

        // Step 2: Set parcel status back to RECEIVED via ParcelService
        try {
            parcelService.updateParcelStatus(parcelId, Constants.ParcelStatus.RECEIVED);
        } catch (Exception e) {
            return "ERROR: Delivery assignment deleted, but failed to revert parcel status: " + e.getMessage();
        }

        // Step 3: Write status log row
        statusLogDAO.addLog(parcelId, Constants.ParcelStatus.RECEIVED, changedBy,
                "Delivery assignment #" + deliveryId + " cancelled; parcel reverted to RECEIVED");

        return "SUCCESS: Delivery #" + deliveryId + " cancelled successfully. Parcel #" +
               parcelId + " reverted to RECEIVED.";
    }

    /**
     * Retrieves the complete status history for a given parcel.
     *
     * @param parcelId ID of the parcel
     * @return list of StatusLog entries ordered oldest first
     */
    public List<StatusLog> getHistoryForParcel(int parcelId) {
        if (parcelId <= 0) {
            return Collections.emptyList();
        }
        return statusLogDAO.getLogsByParcel(parcelId);
    }

    /**
     * Retrieves all delivery assignments for a specific delivery agent.
     *
     * @param agentId ID of the agent
     * @return list of Delivery records
     */
    public List<Delivery> getByAgent(int agentId) {
        if (agentId <= 0) {
            return Collections.emptyList();
        }
        return deliveryDAO.getDeliveriesByAgent(agentId);
    }

    /**
     * Retrieves all deliveries matching a specific status.
     *
     * @param status delivery status to filter
     * @return list of Delivery records
     */
    public List<Delivery> getByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return deliveryDAO.getDeliveriesByStatus(status.trim().toUpperCase());
    }

    /**
     * Retrieves all delivery assignments in the system.
     *
     * @return list of all Delivery records
     */
    public List<Delivery> getAll() {
        return deliveryDAO.getAllDeliveries();
    }

    /**
     * Retrieves the latest delivery record associated with a given parcel.
     *
     * @param parcelId ID of the parcel
     * @return Delivery record or null if none found
     */
    public Delivery getByParcel(int parcelId) {
        if (parcelId <= 0) {
            return null;
        }
        return deliveryDAO.getDeliveryByParcelId(parcelId);
    }

    /**
     * Retrieves a delivery record by primary key ID.
     *
     * @param deliveryId ID of the delivery
     * @return Delivery record or null if not found
     */
    public Delivery getDeliveryById(int deliveryId) {
        if (deliveryId <= 0) {
            return null;
        }
        return deliveryDAO.getDeliveryById(deliveryId);
    }
}
