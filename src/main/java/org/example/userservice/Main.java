package org.example.userservice;

import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.example.userservice.service.UserServiceImpl;
import org.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final UserService userService = new UserServiceImpl();

    public static void main(String[] args) {
        logger.info("Starting User Service Console Application");
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    createUser(scanner);
                    break;
                case "2":
                    findUserById(scanner);
                    break;
                case "3":
                    listAllUsers();
                    break;
                case "4":
                    updateUser(scanner);
                    break;
                case "5":
                    deleteUser(scanner);
                    break;
                case "0":
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        scanner.close();
        HibernateUtil.shutdown();
        logger.info("Application finished.");
    }

    private static void printMenu() {
        System.out.println("\n===== User Management =====");
        System.out.println("1. Create new user");
        System.out.println("2. Find user by ID");
        System.out.println("3. Show all users");
        System.out.println("4. Update user");
        System.out.println("5. Delete user");
        System.out.println("0. Exit");
        System.out.print("Your choice: ");
    }

    private static void createUser(Scanner scanner) {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter age (optional, press Enter to skip): ");
        String ageStr = scanner.nextLine();
        Integer age = null;
        if (!ageStr.isBlank()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid age, using null.");
            }
        }
        try {
            userService.createUser(name, email, age);
            System.out.println("User created successfully.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Create user failed", e);
        }
    }

    private static void findUserById(Scanner scanner) {
        System.out.print("Enter user ID: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                System.out.println(userOpt.get());
            } else {
                System.out.println("User not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Find user failed", e);
        }
    }

    private static void listAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("All users:");
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(Scanner scanner) {
        System.out.print("Enter user ID to update: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            System.out.print("New name (press Enter to keep unchanged): ");
            String name = scanner.nextLine();
            System.out.print("New email (press Enter to keep unchanged): ");
            String email = scanner.nextLine();
            System.out.print("New age (press Enter to keep unchanged): ");
            String ageStr = scanner.nextLine();
            Integer age = null;
            if (!ageStr.isBlank()) {
                age = Integer.parseInt(ageStr);
            }
            userService.updateUser(id, name.isBlank() ? null : name,
                    email.isBlank() ? null : email, age);
            System.out.println("User updated successfully.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID or age format.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Update user failed", e);
        }
    }

    private static void deleteUser(Scanner scanner) {
        System.out.print("Enter user ID to delete: ");
        try {
            Long id = Long.parseLong(scanner.nextLine());
            userService.deleteUser(id);
            System.out.println("User deleted (if existed).");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Delete user failed", e);
        }
    }
}