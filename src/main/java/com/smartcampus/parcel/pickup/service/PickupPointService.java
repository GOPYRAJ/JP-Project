package com.smartcampus.parcel.pickup.service;

import com.smartcampus.parcel.pickup.dao.PickupPointDAO;
import com.smartcampus.parcel.pickup.dao.PickupPointDAOImpl;
import com.smartcampus.parcel.pickup.model.PickupPoint;

import java.util.List;

/**
 * PickupPointService handles business logic and validations for campus pickup locations.
 * Ensures point names are unique, capacity is positive, and points with active parcels
 * cannot be permanently deleted.
 */
public class PickupPointService {

    private final PickupPointDAO pickupPointDAO;

    /**
     * Default constructor initializing default PickupPointDAOImpl.
     */
    public PickupPointService() {
        this(new PickupPointDAOImpl());
    }

    /**
     * Constructor allowing dependency injection of PickupPointDAO.
     *
     * @param pickupPointDAO DAO implementation
     */
    public PickupPointService(PickupPointDAO pickupPointDAO) {
        this.pickupPointDAO = pickupPointDAO;
    }

    /**
     * Validates and adds a new pickup point.
     * Name must be unique and capacity must be strictly greater than 0.
     *
     * @param point the PickupPoint entity to add
     * @return result status message
     */
    public String addPickupPoint(PickupPoint point) {
        if (point == null) {
            return "ERROR: Pickup point details cannot be null.";
        }

        // Validate Name
        if (point.getName() == null || point.getName().trim().isEmpty()) {
            return "ERROR: Pickup point name cannot be empty.";
        }
        String trimmedName = point.getName().trim();
        if (pickupPointDAO.getPickupPointByName(trimmedName) != null) {
            return "ERROR: A pickup point with name '" + trimmedName + "' already exists.";
        }
        point.setName(trimmedName);

        // Validate Location
        if (point.getLocation() == null || point.getLocation().trim().isEmpty()) {
            return "ERROR: Location description cannot be empty.";
        }
        point.setLocation(point.getLocation().trim());

        // Validate Capacity (> 0)
        if (point.getCapacity() <= 0) {
            return "ERROR: Capacity must be a positive integer greater than 0.";
        }

        boolean added = pickupPointDAO.addPickupPoint(point);
        if (added) {
            return "SUCCESS: Pickup point created successfully with ID #" + point.getPointId() + ".";
        } else {
            return "ERROR: Failed to save pickup point to database.";
        }
    }

    /**
     * Retrieves a pickup point by its point ID.
     *
     * @param pointId ID of the pickup point
     * @return PickupPoint entity if found, null otherwise
     */
    public PickupPoint getPickupPointById(int pointId) {
        return pickupPointDAO.getPickupPointById(pointId);
    }

    /**
     * Retrieves all pickup points.
     *
     * @return list of all pickup points
     */
    public List<PickupPoint> getAllPickupPoints() {
        return pickupPointDAO.getAllPickupPoints();
    }

    /**
     * Retrieves all active pickup points.
     *
     * @return list of active pickup points
     */
    public List<PickupPoint> getActivePickupPoints() {
        return pickupPointDAO.getActivePickupPoints();
    }

    /**
     * Validates and updates an existing pickup point.
     *
     * @param point updated PickupPoint entity
     * @return result status message
     */
    public String updatePickupPoint(PickupPoint point) {
        if (point == null) {
            return "ERROR: Pickup point details cannot be null.";
        }

        PickupPoint existing = pickupPointDAO.getPickupPointById(point.getPointId());
        if (existing == null) {
            return "ERROR: Pickup point #" + point.getPointId() + " not found.";
        }

        // Validate Name
        if (point.getName() == null || point.getName().trim().isEmpty()) {
            return "ERROR: Pickup point name cannot be empty.";
        }
        String trimmedName = point.getName().trim();
        PickupPoint nameCheck = pickupPointDAO.getPickupPointByName(trimmedName);
        if (nameCheck != null && nameCheck.getPointId() != point.getPointId()) {
            return "ERROR: A pickup point with name '" + trimmedName + "' already exists.";
        }
        point.setName(trimmedName);

        // Validate Location
        if (point.getLocation() == null || point.getLocation().trim().isEmpty()) {
            return "ERROR: Location description cannot be empty.";
        }
        point.setLocation(point.getLocation().trim());

        // Validate Capacity (> 0)
        if (point.getCapacity() <= 0) {
            return "ERROR: Capacity must be a positive integer greater than 0.";
        }

        boolean updated = pickupPointDAO.updatePickupPoint(point);
        if (updated) {
            return "SUCCESS: Pickup point #" + point.getPointId() + " updated successfully.";
        } else {
            return "ERROR: Failed to update pickup point #" + point.getPointId() + ".";
        }
    }

    /**
     * Deletes a pickup point by ID.
     * If the point still contains parcels not COLLECTED or CANCELLED, deletion is prevented.
     *
     * @param pointId ID of the pickup point to delete
     * @return result status message
     */
    public String deletePickupPoint(int pointId) {
        PickupPoint existing = pickupPointDAO.getPickupPointById(pointId);
        if (existing == null) {
            return "ERROR: Pickup point #" + pointId + " not found.";
        }

        int activeParcels = pickupPointDAO.countActiveParcelsAtPoint(pointId);
        if (activeParcels > 0) {
            return "ERROR: Cannot delete pickup point #" + pointId + " ('" + existing.getName() + "') "
                 + "because it still holds " + activeParcels + " active parcel(s) (not COLLECTED/CANCELLED). "
                 + "Please deactivate the pickup point instead.";
        }

        boolean deleted = pickupPointDAO.deletePickupPoint(pointId);
        if (deleted) {
            return "SUCCESS: Pickup point #" + pointId + " deleted successfully.";
        } else {
            return "ERROR: Failed to delete pickup point #" + pointId + ". It may still be referenced by historical records.";
        }
    }

    /**
     * Activates or deactivates a pickup point.
     *
     * @param pointId ID of the pickup point
     * @param active  true to activate, false to deactivate
     * @return result status message
     */
    public String setActive(int pointId, boolean active) {
        PickupPoint existing = pickupPointDAO.getPickupPointById(pointId);
        if (existing == null) {
            return "ERROR: Pickup point #" + pointId + " not found.";
        }

        boolean updated = pickupPointDAO.setActive(pointId, active);
        if (updated) {
            return "SUCCESS: Pickup point #" + pointId + " ('" + existing.getName() + "') is now "
                 + (active ? "ACTIVE." : "DEACTIVATED.");
        } else {
            return "ERROR: Failed to update status of pickup point #" + pointId + ".";
        }
    }

    /**
     * Checks whether a pickup point has free storage capacity for incoming parcels.
     * Compares the configured capacity with the number of active parcels at that point.
     *
     * @param pointId pickup point ID
     * @return true if operational and active parcels < capacity, false otherwise
     */
    public boolean hasFreeCapacity(int pointId) {
        PickupPoint point = pickupPointDAO.getPickupPointById(pointId);
        if (point == null || !point.isActive()) {
            return false;
        }

        int activeCount = pickupPointDAO.countActiveParcelsAtPoint(pointId);
        return activeCount < point.getCapacity();
    }

    /**
     * Gets the count of active parcels currently assigned to or stored at the pickup point.
     *
     * @param pointId pickup point ID
     * @return count of active parcels
     */
    public int getActiveParcelCount(int pointId) {
        return pickupPointDAO.countActiveParcelsAtPoint(pointId);
    }
}
