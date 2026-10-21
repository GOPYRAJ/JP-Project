package com.smartcampus.parcel.user.ui;

import com.smartcampus.parcel.user.model.User;
import com.smartcampus.parcel.user.service.UserService;
import com.smartcampus.parcel.util.Constants;
import com.smartcampus.parcel.util.InputUtil;

import java.util.List;

/**
 * UserMenu provides console-based user interface for User Management (Module 1).
 * Supports adding, viewing, searching, updating, and deleting campus users.
 */
public class UserMenu {

    private final UserService userService;

    /**
     * Default constructor initializing UserService.
     */
    public UserMenu() {
        this.userService = new UserService();
    }

    /**
     * Constructor allowing dependency injection of UserService.
     *
     * @param userService user service instance
     */
    public UserMenu(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the interactive User Management menu loop.
     */
    public void show() {
        boolean active = true;
        while (active) {
            printMenuHeader();
            int choice = InputUtil.readInt("Enter your choice (0-8)", 0, 8);

            switch (choice) {
                case 1:
                    handleAddUser();
                    break;
                case 2:
                    handleViewAllUsers();
                    break;
                case 3:
                    handleSearchById();
                    break;
                case 4:
                    handleSearchByEmail();
                    break;
                case 5:
                    handleViewByRole();
                    break;
                case 6:
                    handleUpdateUser();
                    break;
                case 7:
                    handleDeleteUser();
                    break;
                case 8:
                    handleLogin();
                    break;
                case 0:
                    active = false;
                    System.out.println("\nReturning to Main Menu...");
                    break;
                default:
                    System.out.println("Invalid selection. Please choose an option from 0 to 8.");
            }
        }
    }

    /**
     * Prints the user management console menu header.
     */
    private void printMenuHeader() {
        System.out.println("\n===============================================================");
        System.out.println("            USER MANAGEMENT MODULE (Module 1)                 ");
        System.out.println("===============================================================");
        System.out.println("  1. Add user");
        System.out.println("  2. View all users");
        System.out.println("  3. Search by ID");
        System.out.println("  4. Search by email");
        System.out.println("  5. View by role");
        System.out.println("  6. Update user");
        System.out.println("  7. Delete user");
        System.out.println("  8. Login user");
        System.out.println("  0. Back to Main Menu");
        System.out.println("---------------------------------------------------------------");
    }

    /**
     * Handles adding a new user through interactive prompts.
     */
    private void handleAddUser() {
        System.out.println("\n--- [1] Add New User ---");

        String fullName = InputUtil.readString("Enter Full Name");
        String email = InputUtil.readString("Enter Email Address");
        String phone = InputUtil.readString("Enter Phone (10 digits)");

        System.out.println("Select Role:");
        System.out.println("  1. STUDENT");
        System.out.println("  2. STAFF");
        System.out.println("  3. ADMIN");
        System.out.println("  4. DELIVERY_AGENT");
        int roleOpt = InputUtil.readInt("Choice (1-4)", 1, 4);
        String role = mapChoiceToRole(roleOpt);

        String department = InputUtil.readString("Enter Department (optional, press Enter to skip)", true);
        String hostelBlock = InputUtil.readString("Enter Hostel/Office Block (optional, press Enter to skip)", true);
        String plainPassword = InputUtil.readString("Enter Password");

        User user = new User(fullName, email, phone, null, role, department, hostelBlock);
        String result = userService.addUser(user, plainPassword);
        System.out.println("\n>>> " + result);
    }

    /**
     * Displays all users in a tabular format.
     */
    private void handleViewAllUsers() {
        System.out.println("\n--- [2] View All Users ---");
        List<User> users = userService.getAllUsers();
        displayUserTable(users);
    }

    /**
     * Searches and displays a user by ID.
     */
    private void handleSearchById() {
        System.out.println("\n--- [3] Search User by ID ---");
        int userId = InputUtil.readInt("Enter User ID");
        User user = userService.getUserById(userId);

        if (user != null) {
            displayUserDetails(user);
        } else {
            System.out.printf(">>> User with ID #%d not found.%n", userId);
        }
    }

    /**
     * Searches and displays a user by email.
     */
    private void handleSearchByEmail() {
        System.out.println("\n--- [4] Search User by Email ---");
        String email = InputUtil.readString("Enter Email Address");
        User user = userService.getUserByEmail(email);

        if (user != null) {
            displayUserDetails(user);
        } else {
            System.out.printf(">>> User with email '%s' not found.%n", email);
        }
    }

    /**
     * Filters and displays users by their role.
     */
    private void handleViewByRole() {
        System.out.println("\n--- [5] View Users by Role ---");
        System.out.println("Select Role to filter:");
        System.out.println("  1. STUDENT");
        System.out.println("  2. STAFF");
        System.out.println("  3. ADMIN");
        System.out.println("  4. DELIVERY_AGENT");
        int roleOpt = InputUtil.readInt("Choice (1-4)", 1, 4);
        String role = mapChoiceToRole(roleOpt);

        List<User> users = userService.getUsersByRole(role);
        System.out.printf("\nUsers with role '%s':%n", role);
        displayUserTable(users);
    }

    /**
     * Handles interactive updating of an existing user.
     */
    private void handleUpdateUser() {
        System.out.println("\n--- [6] Update User Details ---");
        int userId = InputUtil.readInt("Enter User ID to update");
        User existing = userService.getUserById(userId);

        if (existing == null) {
            System.out.printf(">>> User with ID #%d not found.%n", userId);
            return;
        }

        System.out.println("\nCurrent Information:");
        displayUserDetails(existing);
        System.out.println("(Press Enter to keep current value)");

        // Prompt for full name
        String nameInput = InputUtil.readString("Full Name [" + existing.getFullName() + "]", true);
        String fullName = nameInput.isEmpty() ? existing.getFullName() : nameInput;

        // Prompt for email
        String emailInput = InputUtil.readString("Email [" + existing.getEmail() + "]", true);
        String email = emailInput.isEmpty() ? existing.getEmail() : emailInput;

        // Prompt for phone
        String phoneInput = InputUtil.readString("Phone [" + existing.getPhone() + "]", true);
        String phone = phoneInput.isEmpty() ? existing.getPhone() : phoneInput;

        // Prompt for role
        System.out.println("Role [" + existing.getRole() + "] - Change? (1: STUDENT, 2: STAFF, 3: ADMIN, 4: AGENT, Enter: Keep):");
        String roleInput = InputUtil.readString("Role Choice", true);
        String role = existing.getRole();
        if (!roleInput.isEmpty()) {
            try {
                int rChoice = Integer.parseInt(roleInput);
                if (rChoice >= 1 && rChoice <= 4) {
                    role = mapChoiceToRole(rChoice);
                }
            } catch (NumberFormatException ignored) {
                System.out.println("Keeping existing role.");
            }
        }

        // Prompt for department
        String deptCurrent = existing.getDepartment() != null ? existing.getDepartment() : "";
        String deptInput = InputUtil.readString("Department [" + deptCurrent + "]", true);
        String department = deptInput.isEmpty() ? existing.getDepartment() : deptInput;

        // Prompt for hostel block
        String blockCurrent = existing.getHostelBlock() != null ? existing.getHostelBlock() : "";
        String blockInput = InputUtil.readString("Hostel Block [" + blockCurrent + "]", true);
        String hostelBlock = blockInput.isEmpty() ? existing.getHostelBlock() : blockInput;

        // Prompt for new password (optional)
        String newPassword = InputUtil.readString("New Password (optional, press Enter to keep current)", true);

        User updatedUser = new User(existing.getUserId(), fullName, email, phone,
                existing.getPasswordHash(), role, department, hostelBlock, existing.getCreatedAt());

        String result = userService.updateUser(updatedUser, newPassword.isEmpty() ? null : newPassword);
        System.out.println("\n>>> " + result);
    }

    /**
     * Handles safe deletion of a user after confirmation and dependency checks.
     */
    private void handleDeleteUser() {
        System.out.println("\n--- [7] Delete User ---");
        int userId = InputUtil.readInt("Enter User ID to delete");
        User user = userService.getUserById(userId);

        if (user == null) {
            System.out.printf(">>> User with ID #%d not found.%n", userId);
            return;
        }

        System.out.println("\nTarget User for Deletion:");
        displayUserDetails(user);

        String confirm = InputUtil.readString("Are you sure you want to delete this user? (y/N)", true);
        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            String result = userService.deleteUser(userId);
            System.out.println("\n>>> " + result);
        } else {
            System.out.println(">>> Deletion cancelled. No changes made.");
        }
    }

    /**
     * Handles authenticating a user by email and password.
     */
    private void handleLogin() {
        System.out.println("\n--- [8] User Login ---");
        String email = InputUtil.readString("Enter Email Address");
        String password = InputUtil.readString("Enter Password");
        User user = userService.login(email, password);
        if (user != null) {
            System.out.printf(">>> SUCCESS: Login successful! Welcome %s (%s).%n", user.getFullName(), user.getRole());
        } else {
            System.out.println(">>> ERROR: Login failed. Invalid email or password.");
        }
    }

    /**
     * Maps numeric choice (1-4) to Constants.Roles constant.
     */
    private String mapChoiceToRole(int choice) {
        switch (choice) {
            case 1:
                return Constants.Roles.STUDENT;
            case 2:
                return Constants.Roles.STAFF;
            case 3:
                return Constants.Roles.ADMIN;
            case 4:
                return Constants.Roles.DELIVERY_AGENT;
            default:
                return Constants.Roles.STUDENT;
        }
    }

    /**
     * Formats and prints a single user's profile card.
     */
    private void displayUserDetails(User user) {
        System.out.println("---------------------------------------------------------------");
        System.out.printf("  User ID       : %d%n", user.getUserId());
        System.out.printf("  Full Name     : %s%n", user.getFullName());
        System.out.printf("  Email         : %s%n", user.getEmail());
        System.out.printf("  Phone         : %s%n", user.getPhone());
        System.out.printf("  Role          : %s%n", user.getRole());
        System.out.printf("  Department    : %s%n", user.getDepartment() != null ? user.getDepartment() : "-");
        System.out.printf("  Hostel Block  : %s%n", user.getHostelBlock() != null ? user.getHostelBlock() : "-");
        System.out.printf("  Created At    : %s%n", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "-");
        System.out.println("---------------------------------------------------------------");
    }

    /**
     * Formats and prints a table of users.
     */
    private void displayUserTable(List<User> users) {
        if (users == null || users.isEmpty()) {
            System.out.println(">>> No users found.");
            return;
        }

        System.out.println("+-----+----------------------+------------------------------+------------+----------------+----------------------+--------------------+");
        System.out.printf("| %-3s | %-20s | %-28s | %-10s | %-14s | %-20s | %-18s |%n",
                "ID", "Full Name", "Email", "Phone", "Role", "Department", "Hostel Block");
        System.out.println("+-----+----------------------+------------------------------+------------+----------------+----------------------+--------------------+");

        for (User u : users) {
            String name = truncate(u.getFullName(), 20);
            String email = truncate(u.getEmail(), 28);
            String phone = truncate(u.getPhone(), 10);
            String role = truncate(u.getRole(), 14);
            String dept = truncate(u.getDepartment() != null ? u.getDepartment() : "-", 20);
            String block = truncate(u.getHostelBlock() != null ? u.getHostelBlock() : "-", 18);

            System.out.printf("| %-3d | %-20s | %-28s | %-10s | %-14s | %-20s | %-18s |%n",
                    u.getUserId(), name, email, phone, role, dept, block);
        }
        System.out.println("+-----+----------------------+------------------------------+------------+----------------+----------------------+--------------------+");
        System.out.printf("Total Users: %d%n", users.size());
    }

    /**
     * Helper to truncate string if it exceeds maximum column width.
     */
    private String truncate(String text, int maxLen) {
        if (text == null) {
            return "-";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 2) + "..";
    }
}
