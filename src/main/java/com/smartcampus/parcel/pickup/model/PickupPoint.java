package com.smartcampus.parcel.pickup.model;

/**
 * PickupPoint model class representing a record in the 'pickup_points' table.
 * Physical collection locations or lockers across the campus.
 */
public class PickupPoint {

    private int pointId;
    private String name;
    private String location;
    private int capacity;
    private boolean active;

    /**
     * Default no-argument constructor.
     */
    public PickupPoint() {
        this.active = true;
    }

    /**
     * Constructor for creating a new pickup point (before database insertion).
     *
     * @param name     unique location/hub name
     * @param location physical description or campus address
     * @param capacity maximum parcel capacity (must be > 0)
     * @param active   whether the pickup point is operational
     */
    public PickupPoint(String name, String location, int capacity, boolean active) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.active = active;
    }

    /**
     * Full constructor including the generated point ID.
     *
     * @param pointId  unique pickup point primary key
     * @param name     unique location/hub name
     * @param location physical description or campus address
     * @param capacity maximum parcel capacity
     * @param active   whether the pickup point is operational
     */
    public PickupPoint(int pointId, String name, String location, int capacity, boolean active) {
        this.pointId = pointId;
        this.name = name;
        this.location = location;
        this.capacity = capacity;
        this.active = active;
    }

    /**
     * Gets the pickup point ID.
     */
    public int getPointId() {
        return pointId;
    }

    /**
     * Sets the pickup point ID.
     */
    public void setPointId(int pointId) {
        this.pointId = pointId;
    }

    /**
     * Gets the pickup point name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the pickup point name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the physical location description.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the physical location description.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the maximum capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum capacity.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns true if the pickup point is active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active status of the pickup point.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return String.format("PickupPoint [ID=%d, Name='%s', Location='%s', Capacity=%d, Active=%b]",
                pointId, name, location, capacity, active);
    }
}
