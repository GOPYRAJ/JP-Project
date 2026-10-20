package com.smartcampus.parcel.parcel.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Parcel represents a single row in the parcels table.
 * Fields match the database columns exactly (snake_case → camelCase).
 * No SQL or business logic belongs here.
 */
public class Parcel {

    private int parcelId;
    private String trackingNo;
    private String senderName;
    private String courierCompany;
    private int recipientId;
    private int pickupPointId;
    private BigDecimal weightKg;
    private String description;
    private String status;
    private Timestamp receivedAt;
    private Timestamp collectedAt;

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    /** Default no-arg constructor for JDBC result mapping. */
    public Parcel() {}

    /**
     * Full constructor used when inserting a new parcel.
     * parcelId, receivedAt, and collectedAt are set by the database.
     *
     * @param trackingNo     unique courier tracking number
     * @param senderName     name of the sender
     * @param courierCompany courier company name
     * @param recipientId    FK → users.user_id
     * @param pickupPointId  FK → pickup_points.point_id
     * @param weightKg       weight in kilograms (nullable)
     * @param description    optional parcel description
     * @param status         parcel status (see Constants.ParcelStatus)
     */
    public Parcel(String trackingNo, String senderName, String courierCompany,
                  int recipientId, int pickupPointId, BigDecimal weightKg,
                  String description, String status) {
        this.trackingNo      = trackingNo;
        this.senderName      = senderName;
        this.courierCompany  = courierCompany;
        this.recipientId     = recipientId;
        this.pickupPointId   = pickupPointId;
        this.weightKg        = weightKg;
        this.description     = description;
        this.status          = status;
    }

    // -------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------

    public int getParcelId() { return parcelId; }
    public void setParcelId(int parcelId) { this.parcelId = parcelId; }

    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getCourierCompany() { return courierCompany; }
    public void setCourierCompany(String courierCompany) { this.courierCompany = courierCompany; }

    public int getRecipientId() { return recipientId; }
    public void setRecipientId(int recipientId) { this.recipientId = recipientId; }

    public int getPickupPointId() { return pickupPointId; }
    public void setPickupPointId(int pickupPointId) { this.pickupPointId = pickupPointId; }

    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Timestamp receivedAt) { this.receivedAt = receivedAt; }

    public Timestamp getCollectedAt() { return collectedAt; }
    public void setCollectedAt(Timestamp collectedAt) { this.collectedAt = collectedAt; }

    // -------------------------------------------------------
    // toString — useful for debugging / console display
    // -------------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "Parcel[id=%d, tracking=%s, sender=%s, courier=%s, recipientId=%d, " +
            "pickupPointId=%d, weight=%s kg, status=%s, received=%s]",
            parcelId, trackingNo, senderName, courierCompany, recipientId,
            pickupPointId, weightKg, status, receivedAt);
    }
}
