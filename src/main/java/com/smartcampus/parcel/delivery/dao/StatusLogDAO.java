package com.smartcampus.parcel.delivery.dao;

import com.smartcampus.parcel.delivery.model.StatusLog;

import java.util.List;

/**
 * StatusLogDAO defines the data access interface for operations on the 'parcel_status_log' table.
 */
public interface StatusLogDAO {

    /**
     * Inserts an entry into the parcel_status_log table.
     *
     * @param parcelId        ID of the parcel
     * @param status          status string being logged
     * @param changedByUserId ID of the user initiating the status change (nullable / 0 for system)
     * @param remarks         audit remarks
     * @return true if insertion succeeded, false otherwise
     */
    boolean addLog(int parcelId, String status, Integer changedByUserId, String remarks);

    /**
     * Retrieves all status log entries for a parcel, ordered oldest first.
     *
     * @param parcelId ID of the parcel
     * @return list of StatusLog entries ordered chronologically
     */
    List<StatusLog> getLogsByParcel(int parcelId);
}
