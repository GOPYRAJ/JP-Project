package com.smartcampus.parcel.user.model;

import java.sql.Timestamp;

/**
 * User model class representing a record in the 'users' table.
 * Represents students, staff, admins, and delivery agents on campus.
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
    private String department;
    private String hostelBlock;
    private Timestamp createdAt;

    /**
     * Default no-argument constructor.
     */
    public User() {
    }

    /**
     * Constructor for creating a new user (before inserting into the database).
     *
     * @param fullName     the user's full name
     * @param email        the user's email address
     * @param phone        the 10-digit contact phone number
     * @param passwordHash the SHA-256 hashed password
     * @param role         user role (STUDENT, STAFF, ADMIN, DELIVERY_AGENT)
     * @param department   department name (optional)
     * @param hostelBlock  hostel or office block (optional)
     */
    public User(String fullName, String email, String phone, String passwordHash,
                String role, String department, String hostelBlock) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.hostelBlock = hostelBlock;
    }

    /**
     * Constructor with user ID (used when updating an existing user).
     *
     * @param userId       the user's primary key ID
     * @param fullName     the user's full name
     * @param email        the user's email address
     * @param phone        the 10-digit contact phone number
     * @param passwordHash the SHA-256 hashed password
     * @param role         user role
     * @param department   department name
     * @param hostelBlock  hostel or office block
     */
    public User(int userId, String fullName, String email, String phone,
                String passwordHash, String role, String department, String hostelBlock) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.hostelBlock = hostelBlock;
    }

    /**
     * Full constructor including all fields from the database.
     *
     * @param userId       the user's primary key ID
     * @param fullName     the user's full name
     * @param email        the user's email address
     * @param phone        the 10-digit contact phone number
     * @param passwordHash the SHA-256 hashed password
     * @param role         user role
     * @param department   department name
     * @param hostelBlock  hostel or office block
     * @param createdAt    the timestamp when the user account was created
     */
    public User(int userId, String fullName, String email, String phone,
                String passwordHash, String role, String department,
                String hostelBlock, Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.department = department;
        this.hostelBlock = hostelBlock;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    /**
     * Gets the unique user ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the unique user ID.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the full name of the user.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name of the user.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Gets the SHA-256 password hash.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the SHA-256 password hash.
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the user's role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the department.
     */
    public String getDepartment() {
        return department;
    }

    /**
     * Sets the department.
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * Gets the hostel or office block.
     */
    public String getHostelBlock() {
        return hostelBlock;
    }

    /**
     * Sets the hostel or office block.
     */
    public void setHostelBlock(String hostelBlock) {
        this.hostelBlock = hostelBlock;
    }

    /**
     * Gets the account creation timestamp.
     */
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     */
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the User without revealing sensitive details.
     */
    @Override
    public String toString() {
        return String.format("User [ID=%d, Name='%s', Email='%s', Phone='%s', Role='%s', Dept='%s', Block='%s']",
                userId, fullName, email, phone, role,
                (department != null ? department : "-"),
                (hostelBlock != null ? hostelBlock : "-"));
    }
}
