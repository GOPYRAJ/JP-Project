package com.smartcampus.parcel.user.dao;

import com.smartcampus.parcel.user.model.User;

import java.util.List;

/**
 * UserDAO defines the data access contract for operations on the 'users' table,
 * as well as integrity checks against parcels and deliveries tables.
 */
public interface UserDAO {

    /**
     * Inserts a new user record into the database.
     *
     * @param user the User object to add
     * @return true if insertion was successful, false otherwise
     */
    boolean addUser(User user);

    /**
     * Retrieves a user by their primary key user_id.
     *
     * @param userId the ID of the user to look up
     * @return the User object if found, or null
     */
    User getUserById(int userId);

    /**
     * Retrieves a user by their unique email address.
     *
     * @param email the email address to look up
     * @return the User object if found, or null
     */
    User getUserByEmail(String email);

    /**
     * Retrieves all users registered in the system.
     *
     * @return list of User objects
     */
    List<User> getAllUsers();

    /**
     * Retrieves all users having a specific role (e.g. STUDENT, STAFF).
     *
     * @param role the role to filter by
     * @return list of User objects with the specified role
     */
    List<User> getUsersByRole(String role);

    /**
     * Updates an existing user's details.
     *
     * @param user the User object with updated fields
     * @return true if at least one row was updated, false otherwise
     */
    boolean updateUser(User user);

    /**
     * Deletes a user record by their primary key ID.
     *
     * @param userId the ID of the user to delete
     * @return true if the row was deleted, false otherwise
     */
    boolean deleteUser(int userId);

    /**
     * Checks if an email is already registered in the system.
     *
     * @param email the email to verify
     * @return true if the email exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Counts active parcels associated with a recipient (status is not COLLECTED or CANCELLED).
     *
     * @param userId the recipient's user ID
     * @return count of active parcels
     */
    int countActiveParcelsForRecipient(int userId);

    /**
     * Counts active deliveries assigned to a delivery agent (status is ASSIGNED or OUT_FOR_DELIVERY).
     *
     * @param agentId the delivery agent's user ID
     * @return count of active deliveries
     */
    int countActiveDeliveriesForAgent(int agentId);
}
