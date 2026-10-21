package com.smartcampus.parcel.delivery.model;

import java.sql.Timestamp;

/**
 * StatusLog model class representing a record in the 'parcel_status_log' table.
 * Maintains an audit trail tracking every status transition of a parcel.
 */
public class StatusLog {

    private int logId;
    private int parcelId;
    private String status;
    private Integer changedBy;
    private Timestamp changedAt;
    private String remarks;

    /**
     * Default no-argument constructor.
     */
    public StatusLog() {
    }

    /**
     * Constructor for creating a status log record before database insertion.
     *
     * @param parcelId  ID of the parcel
     * @param status    the new status recorded
     * @param changedBy user ID who made the change (or null if system/unknown)
     * @param remarks   audit remarks or description
     */
    public StatusLog(int parcelId, String status, Integer changedBy, String remarks) {
        this.parcelId = parcelId;
        this.status = status;
        this.changedBy = changedBy;
        this.remarks = remarks;
    }

    /**
     * Full-parameter constructor representing an existing database record.
     *
     * @param logId     primary key ID of the log entry
     * @param parcelId  ID of the parcel
     * @param status    status recorded
     * @param changedBy user ID who made the change (or null)
     * @param changedAt timestamp when the change occurred
     * @param remarks   audit remarks
     */
    public StatusLog(int logId, int parcelId, String status, Integer changedBy,
                     Timestamp changedAt, String remarks) {
        this.logId = logId;
        this.parcelId = parcelId;
        this.status = status;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.remarks = remarks;
    }

    // Getters and Setters

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getParcelId() {
        return parcelId;
    }

    public void setParcelId(int parcelId) {
        this.parcelId = parcelId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(Integer changedBy) {
        this.changedBy = changedBy;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "StatusLog{" +
                "logId=" + logId +
                ", parcelId=" + parcelId +
                ", status='" + status + '\'' +
                ", changedBy=" + changedBy +
                ", changedAt=" + changedAt +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}
