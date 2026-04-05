package src;

import java.awt.Font;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ViewBudget {

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
        List<BudgetGoal> goals = new ArrayList<>();
        List<Transaction> trans = new ArrayList<>();
        ViewBudget service = new ViewBudget();

        service.loadData(goals, trans);
        service.manageCategories(goals, trans);

        if (!goals.isEmpty()) {
            service.saveData(goals, trans);
            service.viewOverallBudget(trans, goals);
        } else {
            JOptionPane.showMessageDialog(null, "No categories were created.");
        }

        System.exit(0);
    }

    private void loadData(List<BudgetGoal> goals, List<Transaction> trans) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rsGoals = conn.createStatement().executeQuery("SELECT * FROM BudgetGoals");
            while (rsGoals.next()) {
                goals.add(new BudgetGoal(rsGoals.getString("category_name"), rsGoals.getDouble("total_limit")));
            }
            ResultSet rsTrans = conn.createStatement().executeQuery("SELECT * FROM Transactions");
            while (rsTrans.next()) {
                trans.add(new Transaction(rsTrans.getString("category_name"), rsTrans.getDouble("amount")));
            }
        } catch (SQLException e) {
        }
    }

    private void saveData(List<BudgetGoal> goals, List<Transaction> trans) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            conn.createStatement().executeUpdate("DELETE FROM Transactions");
            conn.createStatement().executeUpdate("DELETE FROM BudgetGoals");

            PreparedStatement psGoal = conn.prepareStatement("INSERT INTO BudgetGoals (category_name, total_limit) VALUES (?, ?)");
            for (BudgetGoal g : goals) {
                psGoal.setString(1, g.category);
                psGoal.setDouble(2, g.totalLimit);
                psGoal.executeUpdate();
            }

            PreparedStatement psTrans = conn.prepareStatement("INSERT INTO Transactions (category_name, amount) VALUES (?, ?)");
            for (Transaction t : trans) {
                psTrans.setString(1, t.category);
                psTrans.setDouble(2, t.amount);
                psTrans.executeUpdate();
            }
        } catch (SQLException e) {
        }
    }

    private String getCategoryIcon(String category) {
        String lower = category.toLowerCase();
        if (lower.contains("food")) return "🍔";
        if (lower.contains("transport")) return "🚌";
        if (lower.contains("grocery") || lower.contains("shopping")) return "🛒";
        if (lower.contains("essential")) return "📦";
        if (lower.contains("school") || lower.contains("supplies")) return "✏️";
        if (lower.contains("tuition") || lower.contains("education")) return "🎓";
        if (lower.contains("utility")) return "💡";
        if (lower.contains("health")) return "💊";
        if (lower.contains("entertainment")) return "🎮";
        return "📌";
    }

    public void manageCategories(List<BudgetGoal> goals, List<Transaction> transactions) {
        boolean running = true;
        while (running) {
            String[] options = {"Add New Category", "Add Spending", "Finish & View Report"};
            int choice = JOptionPane.showOptionDialog(null, "Select an action:", "Budget Manager",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                String name = JOptionPane.showInputDialog("Enter New Category Name:");
                if (name != null && !name.trim().isEmpty()) {
                    String limitStr = JOptionPane.showInputDialog("Enter Limit for " + name + ":");
                    if (limitStr == null) continue;
                    try {
                        double limit = Double.parseDouble(limitStr);
                        goals.add(new BudgetGoal(name.trim(), limit));
                    } catch (NumberFormatException e) {
                    }
                }
            } else if (choice == 1) {
                if (goals.isEmpty()) continue;
                String[] categoryNames = new String[goals.size()];
                for (int i = 0; i < goals.size(); i++) categoryNames[i] = goals.get(i).category;
                
                String selected = (String) JOptionPane.showInputDialog(null, "Select Category:", "Add Spending",
                        JOptionPane.QUESTION_MESSAGE, null, categoryNames, categoryNames[0]);
                if (selected != null) {
                    String spentStr = JOptionPane.showInputDialog("Amount spent on " + selected + ":");
                    if (spentStr == null) continue;
                    try {
                        double spent = Double.parseDouble(spentStr);
                        transactions.add(new Transaction(selected, spent));
                    } catch (NumberFormatException e) {
                    }
                }
            } else {
                running = false;
            }
        }
    }

    public void viewOverallBudget(List<Transaction> transactions, List<BudgetGoal> goals) {
        StringBuilder tableReport = new StringBuilder();
        tableReport.append("BUDGET REPORT\n");
        tableReport.append("============================================================\n");
        tableReport.append(String.format("%-20s | %-12s | %-10s | %-10s\n", "Category", "Limit", "Spent", "Remaining"));
        tableReport.append("------------------------------------------------------------\n");

        double totalLimit = 0, totalSpent = 0;
        boolean hasLowBalance = false;
        StringBuilder categoryAlerts = new StringBuilder();

        for (BudgetGoal goal : goals) {
            double spent = 0;
            for (Transaction t : transactions) {
                if (t.category.equalsIgnoreCase(goal.category)) spent += t.amount;
            }
            double remaining = goal.totalLimit - spent;
            totalLimit += goal.totalLimit;
            totalSpent += spent;
            String icon = getCategoryIcon(goal.category);

            tableReport.append(String.format("%-20s | ₱%-11.2f | ₱%-9.2f | ₱%-9.2f\n",
                    icon + " " + goal.category, goal.totalLimit, spent, remaining));

            if (remaining <= 100) {
                hasLowBalance = true;
                categoryAlerts.append("<html><font color='red'>⚠️ ")
                        .append(icon).append(" ")
                        .append(goal.category)
                        .append(": ₱")
                        .append(String.format("%.2f", remaining))
                        .append(" LEFT</font></html><br>");
            }
        }

        tableReport.append("============================================================\n");
        tableReport.append(String.format("TOTAL                | ₱%-11.2f | ₱%-9.2f | ₱%-9.2f\n",
                totalLimit, totalSpent, (totalLimit - totalSpent)));

        if (hasLowBalance) {
            String redWarning = "<html><font color='red'>⚠️ WARNING: Some categories have low balance (₱100 or less).</font></html>";
            JOptionPane.showMessageDialog(null, redWarning, "CAUTION", JOptionPane.WARNING_MESSAGE);
        }

        JTextArea area = new JTextArea(tableReport.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(null, area, "Final Report", JOptionPane.INFORMATION_MESSAGE);

        if (hasLowBalance) {
            JOptionPane.showMessageDialog(null, categoryAlerts.toString(), "Low Balance Alerts", JOptionPane.ERROR_MESSAGE);
        }
    }
}
