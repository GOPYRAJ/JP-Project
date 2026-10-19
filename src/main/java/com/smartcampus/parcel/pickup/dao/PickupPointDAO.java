package com.smartcampus.parcel.pickup.dao;

import com.smartcampus.parcel.pickup.model.PickupPoint;

import java.util.List;

/**
 * PickupPointDAO defines the data access contract for operations on the 'pickup_points' table,
 * as well as integrity checks against parcels stored at pickup points.
 */
public interface PickupPointDAO {

    /**
     * Inserts a new pickup point into the database.
     *
     * @param point the PickupPoint object to add
     * @return true if insertion was successful, false otherwise
     */
    boolean addPickupPoint(PickupPoint point);

    /**
     * Retrieves a pickup point by its primary key point_id.
     *
     * @param pointId the ID of the pickup point
     * @return the PickupPoint object if found, or null
     */
    PickupPoint getPickupPointById(int pointId);

    /**
     * Retrieves a pickup point by its unique name.
     *
     * @param name the name of the pickup point
     * @return the PickupPoint object if found, or null
     */
    PickupPoint getPickupPointByName(String name);

    /**
     * Retrieves all pickup points.
     *
     * @return list of all PickupPoint objects
     */
    List<PickupPoint> getAllPickupPoints();

    /**
     * Retrieves only operational (active) pickup points.
     *
     * @return list of active PickupPoint objects
     */
    List<PickupPoint> getActivePickupPoints();

    /**
     * Updates an existing pickup point's details.
     *
     * @param point the PickupPoint object with updated details
     * @return true if at least one row was updated, false otherwise
     */
    boolean updatePickupPoint(PickupPoint point);

    /**
     * Deletes a pickup point record by ID.
     *
     * @param pointId the ID of the pickup point to delete
     * @return true if the row was deleted, false otherwise
     */
    boolean deletePickupPoint(int pointId);

    /**
     * Sets the active status of a pickup point.
     *
     * @param pointId the ID of the pickup point
     * @param active  new active status
     * @return true if status was updated, false otherwise
     */
    boolean setActive(int pointId, boolean active);

    /**
     * Counts active parcels currently designated for this pickup point
     * (status is not COLLECTED and not CANCELLED).
     *
     * @param pointId the ID of the pickup point
     * @return count of active parcels
     */
    int countActiveParcelsAtPoint(int pointId);
}
