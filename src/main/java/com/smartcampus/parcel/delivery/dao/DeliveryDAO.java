package com.smartcampus.parcel.delivery.dao;

import com.smartcampus.parcel.delivery.model.Delivery;

import java.util.List;

/**
 * DeliveryDAO defines the data access interface for operations on the 'deliveries' table.
 */
public interface DeliveryDAO {

    /**
     * Inserts a new delivery record into the database and populates its generated deliveryId.
     *
     * @param delivery the Delivery object to insert
     * @return true if insertion succeeded, false otherwise
     */
    boolean addDelivery(Delivery delivery);

    /**
     * Retrieves a delivery record by its primary key ID.
     *
     * @param deliveryId the ID of the delivery
     * @return the Delivery object if found, or null
     */
    Delivery getDeliveryById(int deliveryId);

    /**
     * Retrieves the latest delivery record associated with a given parcel ID.
     *
     * @param parcelId the ID of the parcel
     * @return the Delivery object if found, or null
     */
    Delivery getDeliveryByParcelId(int parcelId);

    /**
     * Retrieves all delivery records assigned to a specific delivery agent.
     *
     * @param agentId the user ID of the delivery agent
     * @return list of Delivery records assigned to the agent
     */
    List<Delivery> getDeliveriesByAgent(int agentId);

    /**
     * Retrieves all delivery records matching a specific delivery status.
     *
     * @param status the delivery status to filter by
     * @return list of Delivery records with the specified status
     */
    List<Delivery> getDeliveriesByStatus(String status);

    /**
     * Retrieves all delivery records in the system ordered newest first.
     *
     * @return list of all Delivery records
     */
    List<Delivery> getAllDeliveries();

    /**
     * Updates the status of a delivery record.
     * If the new status is DELIVERED, delivered_at is automatically updated to the current timestamp.
     *
     * @param deliveryId the ID of the delivery
     * @param status     the new delivery status
     * @return true if at least one row was updated, false otherwise
     */
    boolean updateDeliveryStatus(int deliveryId, String status);

    /**
     * Updates the remarks for a delivery record.
     *
     * @param deliveryId the ID of the delivery
     * @param remarks    the new remarks text
     * @return true if at least one row was updated, false otherwise
     */
    boolean updateRemarks(int deliveryId, String remarks);

    /**
     * Deletes a delivery record by ID.
     *
     * @param deliveryId the ID of the delivery to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteDelivery(int deliveryId);

    /**
     * Counts the number of active delivery assignments for an agent (status ASSIGNED or OUT_FOR_DELIVERY).
     *
     * @param agentId the user ID of the delivery agent
     * @return count of active deliveries
     */
    int countActiveDeliveriesForAgent(int agentId);
}
