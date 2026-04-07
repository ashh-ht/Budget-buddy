package com.budgetbuddy;

import java.util.Scanner;
import java.sql.*;

public class View_Budget {
    private static Scanner sc = new Scanner(System.in);

    public boolean showBudgetMenu() {
        if (Session.getCurrentUser() == null) {
            System.out.println("Access Denied. Please log in.");
            return false;
        }

        boolean active = true;
        while (active) {
            System.out.println("\n--- BUDGET & ACCOUNT SETTINGS ---");
            System.out.println("[1] Add Category");
            System.out.println("[2] Add Spending");
            System.out.println("[3] View Report");
            System.out.println("[4] Edit Card Details");
            System.out.println("[5] Return to Main Menu");
            System.out.print("Choice: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1": addCategory(); break;
                case "2": addSpending(); break;
                case "3": viewReport(); break;
                case "4": editCardDetails(); break;
                case "5": active = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        return true;
    }

    private void editCardDetails() {
        String cardNum = Account.getCardNum(); 
        
        System.out.println("\nEdit: [1] Card Name [2] Card PIN");
        System.out.print("Choice: ");
        String choice = sc.nextLine();

        try (Connection conn = dbConnection.getConnection()) {
            if (choice.equals("1")) {
                System.out.print("New First Name: ");
                String fName = sc.nextLine();
                System.out.print("New Last Name: ");
                String lName = sc.nextLine();

                String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE card_num = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, fName);
                ps.setString(2, lName);
                ps.setString(3, cardNum);
                ps.executeUpdate();
                System.out.println("Name updated.");
            } else if (choice.equals("2")) {
                System.out.print("New PIN: ");
                String pin = sc.nextLine();
                String sql = "UPDATE users SET card_pin = ? WHERE card_num = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, pin);
                ps.setString(2, cardNum);
                ps.executeUpdate();
                System.out.println("PIN updated.");
            }
        } catch (SQLException e) {
            System.out.println("Update failed.");
        }
    }

    private void viewReport() {
        System.out.printf("\n%-20s | %-12s | %-12s\n", "Category", "Limit", "Spent");
        String sql = "SELECT g.category_name, g.total_limit, IFNULL(SUM(t.amount), 0) as spent " +
                     "FROM BudgetGoals g LEFT JOIN Transactions t " +
                     "ON g.category_name = t.category_name AND g.username = t.username " +
                     "WHERE g.username = ? GROUP BY g.category_name";

        try (Connection conn = dbConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Session.getCurrentUser());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("%-20s | PHP%-10.2f | PHP%-10.2f\n", 
                    rs.getString("category_name"), rs.getDouble("total_limit"), rs.getDouble("spent"));
            }
        } catch (SQLException e) { System.out.println("Error."); }
    }

    private void addCategory() {
        System.out.print("Category: ");
        String name = sc.nextLine();
        System.out.print("Limit: ");
        double lim = Double.parseDouble(sc.nextLine());
        executeBudgetUpdate("INSERT INTO BudgetGoals (category_name, total_limit, username) VALUES (?, ?, ?)", name, lim);
    }

    private void addSpending() {
        System.out.print("Category: ");
        String name = sc.nextLine();
        System.out.print("Amount: ");
        double amt = Double.parseDouble(sc.nextLine());
        executeBudgetUpdate("INSERT INTO Transactions (category_name, amount, username) VALUES (?, ?, ?)", name, amt);
    }

    private void executeBudgetUpdate(String sql, String cat, double val) {
        try (Connection conn = dbConnection.getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cat);
            ps.setDouble(2, val);
            ps.setString(3, Session.getCurrentUser());
            ps.executeUpdate();
            System.out.println("Success.");
        } catch (SQLException e) { System.out.println("Database Error."); }
    }
}