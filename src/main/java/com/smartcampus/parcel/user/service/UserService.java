package com.smartcampus.parcel.user.service;

import com.smartcampus.parcel.user.dao.UserDAO;
import com.smartcampus.parcel.user.dao.UserDAOImpl;
import com.smartcampus.parcel.user.model.User;
import com.smartcampus.parcel.util.Constants;
import com.smartcampus.parcel.util.PasswordUtil;

import java.util.List;
import java.util.regex.Pattern;

/**
 * UserService provides business logic and validation for user operations.
 * Validates data integrity before calling the DAO layer.
 */
public class UserService {

    private final UserDAO userDAO;

    // Email regex: standard RFC-compliant pattern for student/campus emails
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    /**
     * Default constructor using UserDAOImpl.
     */
    public UserService() {
        this(new UserDAOImpl());
    }

    /**
     * Constructor allowing dependency injection of UserDAO.
     *
     * @param userDAO UserDAO implementation
     */
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Validates and registers a new user with a hashed password.
     *
     * @param user          User entity containing user details
     * @param plainPassword raw password to be hashed
     * @return clear result message suitable for display in UI
     */
    public String addUser(User user, String plainPassword) {
        if (user == null) {
            return "ERROR: User data cannot be null.";
        }

        // Validate Full Name
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "ERROR: Full name cannot be empty.";
        }

        // Validate Email
        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        if (!isValidEmail(email)) {
            return "ERROR: Invalid email format. Example: student@smartcampus.edu";
        }
        if (userDAO.emailExists(email)) {
            return "ERROR: An account with email '" + email + "' already exists.";
        }
        user.setEmail(email);

        // Validate Phone (exactly 10 digits)
        String phone = user.getPhone() != null ? user.getPhone().trim() : "";
        if (!isValidPhone(phone)) {
            return "ERROR: Phone number must be exactly 10 numeric digits.";
        }
        user.setPhone(phone);

        // Validate Role against Constants.Roles
        if (!isValidRole(user.getRole())) {
            return "ERROR: Invalid role '" + user.getRole() + "'. Allowed roles: STUDENT, STAFF, ADMIN, DELIVERY_AGENT.";
        }

        // Validate and hash password
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            return "ERROR: Password cannot be empty.";
        }
        user.setPasswordHash(PasswordUtil.hash(plainPassword));

        // Save to database
        boolean added = userDAO.addUser(user);
        if (added) {
            return "SUCCESS: User registered successfully with ID #" + user.getUserId() + ".";
        } else {
            return "ERROR: Failed to save user to the database.";
        }
    }

    /**
     * Alias for addUser to provide registration semantics.
     */
    public String registerUser(User user, String plainPassword) {
        return addUser(user, plainPassword);
    }

    /**
     * Authenticates a user using email and plain-text password.
     *
     * @param email         user email
     * @param plainPassword plain-text password
     * @return User object if authentication succeeds, null otherwise
     */
    public User login(String email, String plainPassword) {
        if (email == null || plainPassword == null) {
            return null;
        }
        User user = userDAO.getUserByEmail(email.trim());
        if (user == null) {
            return null;
        }
        if (PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId user ID
     * @return User object or null if not found
     */
    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email user email
     * @return User object or null if not found
     */
    public User getUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return userDAO.getUserByEmail(email.trim());
    }

    /**
     * Retrieves all registered users.
     *
     * @return list of User entities
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Retrieves users belonging to a specific role.
     *
     * @param role role name
     * @return list of User entities
     */
    public List<User> getUsersByRole(String role) {
        if (role == null) {
            return List.of();
        }
        return userDAO.getUsersByRole(role.trim().toUpperCase());
    }

    /**
     * Updates an existing user's profile and optionally resets their password.
     *
     * @param user             User object with updated fields
     * @param newPlainPassword new plain password (if empty or null, password remains unchanged)
     * @return clear result message suitable for UI
     */
    public String updateUser(User user, String newPlainPassword) {
        if (user == null) {
            return "ERROR: User data cannot be null.";
        }

        User existing = userDAO.getUserById(user.getUserId());
        if (existing == null) {
            return "ERROR: User with ID #" + user.getUserId() + " not found.";
        }

        // Validate Full Name
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            return "ERROR: Full name cannot be empty.";
        }

        // Validate Email
        String email = user.getEmail() != null ? user.getEmail().trim() : "";
        if (!isValidEmail(email)) {
            return "ERROR: Invalid email format.";
        }
        // If email changed, ensure new email is not taken by another user
        if (!email.equalsIgnoreCase(existing.getEmail()) && userDAO.emailExists(email)) {
            return "ERROR: Email '" + email + "' is already in use by another user.";
        }
        user.setEmail(email);

        // Validate Phone (10 digits)
        String phone = user.getPhone() != null ? user.getPhone().trim() : "";
        if (!isValidPhone(phone)) {
            return "ERROR: Phone number must be exactly 10 numeric digits.";
        }
        user.setPhone(phone);

        // Validate Role
        if (!isValidRole(user.getRole())) {
            return "ERROR: Invalid role. Allowed roles: STUDENT, STAFF, ADMIN, DELIVERY_AGENT.";
        }

        // Update password hash if a new plain password was supplied
        if (newPlainPassword != null && !newPlainPassword.trim().isEmpty()) {
            user.setPasswordHash(PasswordUtil.hash(newPlainPassword));
        } else {
            user.setPasswordHash(existing.getPasswordHash());
        }

        boolean updated = userDAO.updateUser(user);
        if (updated) {
            return "SUCCESS: User #" + user.getUserId() + " (" + user.getFullName() + ") updated successfully.";
        } else {
            return "ERROR: Failed to update user in the database.";
        }
    }

    /**
     * Updates an existing user without modifying the password.
     */
    public String updateUser(User user) {
        return updateUser(user, null);
    }

    /**
     * Deletes a user with business rule validations:
     * - Refuses if user has active parcels (not COLLECTED or CANCELLED) as a recipient.
     * - Refuses if user is a delivery agent with an active delivery (ASSIGNED or OUT_FOR_DELIVERY).
     *
     * @param userId ID of the user to delete
     * @return clear result message suitable for UI
     */
    public String deleteUser(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            return "ERROR: User with ID #" + userId + " does not exist.";
        }

        // Rule: check active parcels for recipient
        int activeParcels = userDAO.countActiveParcelsForRecipient(userId);
        if (activeParcels > 0) {
            return "CANNOT DELETE: User #" + userId + " (" + user.getFullName() + ") has "
                    + activeParcels + " active parcel(s) awaiting collection/delivery. Status must be COLLECTED or CANCELLED.";
        }

        // Rule: check active deliveries for delivery agent
        int activeDeliveries = userDAO.countActiveDeliveriesForAgent(userId);
        if (activeDeliveries > 0) {
            return "CANNOT DELETE: User #" + userId + " (" + user.getFullName() + ") has "
                    + activeDeliveries + " active delivery assignment(s). Complete or reassign them before deletion.";
        }

        boolean deleted = userDAO.deleteUser(userId);
        if (deleted) {
            return "SUCCESS: User #" + userId + " (" + user.getFullName() + ") was deleted successfully.";
        } else {
            return "ERROR: Failed to delete user from database (foreign key constraint or server error).";
        }
    }

    /**
     * Helper to check if an email format is valid.
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Helper to check if a phone number contains exactly 10 numeric digits.
     */
    public boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^\\d{10}$");
    }

    /**
     * Helper to check if a role is valid per Constants.Roles.
     */
    public boolean isValidRole(String role) {
        if (role == null) {
            return false;
        }
        String r = role.trim().toUpperCase();
        return r.equals(Constants.Roles.STUDENT)
                || r.equals(Constants.Roles.STAFF)
                || r.equals(Constants.Roles.ADMIN)
                || r.equals(Constants.Roles.DELIVERY_AGENT);
    }
}
