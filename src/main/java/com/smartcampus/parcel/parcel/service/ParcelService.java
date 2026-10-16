package com.smartcampus.parcel.parcel.service;

/**
 * ParcelService handles business operations on parcels.
 * Module 2 will implement full CRUD and validation here.
 */
public class ParcelService {

    /**
     * Updates the status of a parcel and writes to parcel_status_log.
     * Stub method to allow Module 3 (Delivery) to invoke it from day one.
     *
     * @param parcelId id of the parcel
     * @param status new parcel status
     */
    public void updateParcelStatus(int parcelId, String status) {
        // Module 2 will implement full database update and status log recording
        System.out.printf("[ParcelService Stub] Updating parcel #%d status to: %s%n", parcelId, status);
    }
}
