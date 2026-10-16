package com.smartcampus.parcel.util;

/**
 * Constants holds string constants matching the database ENUM values.
 * Team members should use these constants across all modules to avoid typos.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // ==========================================
    // USER ROLES (Module 1 - users.role)
    // ==========================================
    public static final class Roles {
        public static final String STUDENT = "STUDENT";
        public static final String STAFF = "STAFF";
        public static final String ADMIN = "ADMIN";
        public static final String DELIVERY_AGENT = "DELIVERY_AGENT";

        private Roles() {}
    }

    // ==========================================
    // PARCEL STATUSES (Module 2 - parcels.status)
    // Flow: RECEIVED -> ASSIGNED -> OUT_FOR_DELIVERY -> DELIVERED -> COLLECTED
    // Alternates: FAILED, CANCELLED
    // ==========================================
    public static final class ParcelStatus {
        public static final String RECEIVED = "RECEIVED";
        public static final String ASSIGNED = "ASSIGNED";
        public static final String OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
        public static final String DELIVERED = "DELIVERED";
        public static final String COLLECTED = "COLLECTED";
        public static final String FAILED = "FAILED";
        public static final String CANCELLED = "CANCELLED";

        private ParcelStatus() {}
    }

    // ==========================================
    // DELIVERY STATUSES (Module 3 - deliveries.status)
    // ==========================================
    public static final class DeliveryStatus {
        public static final String ASSIGNED = "ASSIGNED";
        public static final String OUT_FOR_DELIVERY = "OUT_FOR_DELIVERY";
        public static final String DELIVERED = "DELIVERED";
        public static final String FAILED = "FAILED";

        private DeliveryStatus() {}
    }
}
