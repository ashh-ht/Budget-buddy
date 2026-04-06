package com.budgetbuddy;

import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class View_Budget {

    private static final String URL = "jdbc:mysql://localhost:3306/budgetbuddyproject";
    private static final String USER = "root"; 
    private static final String PASS = "budgetbuddy-comprog"; 

    public static class Transaction {
        public String category;
        public double amount;

        public Transaction(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
    }

    public static class BudgetGoal {
        public String category;
        public double totalLimit;

        public BudgetGoal(String category, double totalLimit) {
            this.category = category;
            this.totalLimit = totalLimit;
        }
    }

    public static void main(String[] args) {
        if (Session.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(null, "Access Denied. Please log in.", "Session Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        List<BudgetGoal> goals = new ArrayList<>();
        List<Transaction> trans = new ArrayList<>();
        View_Budget service = new View_Budget();

        service.loadData(goals, trans);
        service.manageCategories(goals, trans);

        if (!goals.isEmpty()) {
            service.saveData(goals, trans);
            service.viewOverallBudget(trans, goals);
        }

        System.exit(0);
    }

    private void loadData(List<BudgetGoal> goals, List<Transaction> trans) {
        String currentUser = Session.getCurrentUser();
        String goalSql = "SELECT category_name, total_limit FROM BudgetGoals WHERE username = ?";
        String transSql = "SELECT category_name, amount FROM Transactions WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            try (PreparedStatement ps = conn.prepareStatement(goalSql)) {
                ps.setString(1, currentUser);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    goals.add(new BudgetGoal(rs.getString("category_name"), rs.getDouble("total_limit")));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(transSql)) {
                ps.setString(1, currentUser);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    trans.add(new Transaction(rs.getString("category_name"), rs.getDouble("amount")));
                }
            }
        } catch (SQLException e) {}
    }

    private void saveData(List<BudgetGoal> goals, List<Transaction> trans) {
        String currentUser = Session.getCurrentUser();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            PreparedStatement delT = conn.prepareStatement("DELETE FROM Transactions WHERE username = ?");
            delT.setString(1, currentUser);
            delT.executeUpdate();

            PreparedStatement delG = conn.prepareStatement("DELETE FROM BudgetGoals WHERE username = ?");
            delG.setString(1, currentUser);
            delG.executeUpdate();

            PreparedStatement psGoal = conn.prepareStatement("INSERT INTO BudgetGoals (category_name, total_limit, username) VALUES (?, ?, ?)");
            for (BudgetGoal g : goals) {
                psGoal.setString(1, g.category);
                psGoal.setDouble(2, g.totalLimit);
                psGoal.setString(3, currentUser);
                psGoal.executeUpdate();
            }

            PreparedStatement psTrans = conn.prepareStatement("INSERT INTO Transactions (category_name, amount, username) VALUES (?, ?, ?)");
            for (Transaction t : trans) {
                psTrans.setString(1, t.category);
                psTrans.setDouble(2, t.amount);
                psTrans.setString(3, currentUser);
                psTrans.executeUpdate();
            }
        } catch (SQLException e) {}
    }

    public void manageCategories(List<BudgetGoal> goals, List<Transaction> transactions) {
        boolean running = true;
        while (running) {
            String[] options = {"Add Category", "Add Spending", "View Report"};
            int choice = JOptionPane.showOptionDialog(null, "User: " + Session.getCurrentUser(), "Budget Buddy",
                    0, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                String name = JOptionPane.showInputDialog("Category Name:");
                if (name != null && !name.trim().isEmpty()) {
                    String lim = JOptionPane.showInputDialog("Limit (PHP):");
                    try {
                        goals.add(new BudgetGoal(name.trim(), Double.parseDouble(lim)));
                    } catch (Exception e) {}
                }
            } else if (choice == 1) {
                if (goals.isEmpty()) continue;
                String[] names = goals.stream().map(g -> g.category).toArray(String[]::new);
                String sel = (String) JOptionPane.showInputDialog(null, "Category:", "Spending", 3, null, names, names[0]);
                if (sel != null) {
                    String amt = JOptionPane.showInputDialog("Amount (PHP):");
                    try {
                        transactions.add(new Transaction(sel, Double.parseDouble(amt)));
                    } catch (Exception e) {}
                }
            } else {
                running = false;
            }
        }
    }

    public void viewOverallBudget(List<Transaction> transactions, List<BudgetGoal> goals) {
        StringBuilder sb = new StringBuilder("BUDGET REPORT: " + Session.getCurrentUser() + "\n\n");
        sb.append(String.format("%-20s | %-12s | %-12s\n", "Category", "Limit", "Spent"));
        for (BudgetGoal g : goals) {
            double s = transactions.stream()
                                   .filter(t -> t.category.equals(g.category))
                                   .mapToDouble(t -> t.amount)
                                   .sum();
            sb.append(String.format("%-20s | PHP%-9.2f | PHP%-9.2f\n", g.category, g.totalLimit, s));
        }
        JTextArea txt = new JTextArea(sb.toString());
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setEditable(false);
        JOptionPane.showMessageDialog(null, txt);
    }
}

class Session {
    private static String user = null;
    public static void login(String u) { user = u; }
    public static String getCurrentUser() { return user; }
}