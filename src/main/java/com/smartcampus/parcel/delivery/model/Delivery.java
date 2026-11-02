package com.smartcampus.parcel.delivery.model;

import java.sql.Timestamp;

/**
 * Delivery model class representing a record in the 'deliveries' table.
 * Tracks assignments of parcels to campus delivery agents and their delivery status.
 */
public class Delivery {

    private int deliveryId;
    private int parcelId;
    private int agentId;
    private String status;
    private Timestamp assignedAt;
    private Timestamp deliveredAt;
    private String remarks;

    /**
     * Default no-argument constructor.
     */
    public Delivery() {
    }

    /**
     * Constructor for creating a new delivery assignment before database insertion.
     *
     * @param parcelId ID of the parcel being delivered
     * @param agentId  ID of the delivery agent
     * @param status   initial delivery status (typically ASSIGNED)
     * @param remarks  optional notes regarding the delivery
     */
    public Delivery(int parcelId, int agentId, String status, String remarks) {
        this.parcelId = parcelId;
        this.agentId = agentId;
        this.status = status;
        this.remarks = remarks;
    }

    /**
     * Full-parameter constructor representing an existing database record.
     *
     * @param deliveryId  primary key ID of the delivery
     * @param parcelId    ID of the parcel
     * @param agentId     ID of the delivery agent
     * @param status      current delivery status
     * @param assignedAt  timestamp when delivery was assigned
     * @param deliveredAt timestamp when delivery was completed (or null)
     * @param remarks     delivery remarks or notes
     */
    public Delivery(int deliveryId, int parcelId, int agentId, String status,
                    Timestamp assignedAt, Timestamp deliveredAt, String remarks) {
        this.deliveryId = deliveryId;
        this.parcelId = parcelId;
        this.agentId = agentId;
        this.status = status;
        this.assignedAt = assignedAt;
        this.deliveredAt = deliveredAt;
        this.remarks = remarks;
    }

    // Getters and Setters

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public int getParcelId() {
        return parcelId;
    }

    public void setParcelId(int parcelId) {
        this.parcelId = parcelId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Timestamp getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Timestamp deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "deliveryId=" + deliveryId +
                ", parcelId=" + parcelId +
                ", agentId=" + agentId +
                ", status='" + status + '\'' +
                ", assignedAt=" + assignedAt +
                ", deliveredAt=" + deliveredAt +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
