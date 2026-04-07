package com.budgetbuddy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import javax.swing.JOptionPane;

public class CardManager {

    private final String url = "jdbc:mysql://localhost:3306/budgetbuddyproject";
    private final String user = "root";
    private final String password = "budgetbuddy-comprog";

    public void updateCardNum(String oldCardNum, String newCardNum) {
        String sql = "UPDATE cardholder SET card_num = ? WHERE card_num = ?";
        executeDatabaseUpdate(sql, newCardNum, oldCardNum);
    }

    public void updateCardPin(String cardNum, String newPin) {
        String sql = "UPDATE card SET card_pin = ? WHERE card_num = ?";
        executeDatabaseUpdate(sql, newPin, cardNum);
    }

    private void executeDatabaseUpdate(String sql, String newValue, String cardIdentifier) {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newValue);
            pstmt.setString(2, cardIdentifier);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "<html><font color='green'>✅ Success!</font></html>");
            } else {
                JOptionPane.showMessageDialog(null, "<html><font color='red'>❌ Record not found.</font></html>");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        CardManager manager = new CardManager();

        String[] options = {"Card Number", "PIN"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select field to edit:",
                "Edit Card Details",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == -1) return;

        String currentCard = JOptionPane.showInputDialog("Enter Current Card Number:");
        if (currentCard == null || currentCard.isEmpty()) return;

        if (choice == 0) {
            String newNum = JOptionPane.showInputDialog("Enter New Card Number:");
            if (newNum != null && !newNum.isEmpty()) {
                manager.updateCardNum(currentCard, newNum);
            }
        } else if (choice == 1) {
            String newPin = JOptionPane.showInputDialog("Enter New PIN:");
            if (newPin != null && !newPin.isEmpty()) {
                manager.updateCardPin(currentCard, newPin);
            }
        }

        System.exit(0);
    }
}