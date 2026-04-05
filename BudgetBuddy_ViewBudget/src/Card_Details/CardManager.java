package Card_Details;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class CardManager {

    private final String url = "jdbc:mysql://localhost:3306/budgetbuddyproject";
    private final String user = "root";
    private final String password = "";

    public void updateCardDetails(String oldCardNum, String newCardNum, String newPin) {
        String sql = "UPDATE card SET card_num = ?, card_pin = ? WHERE card_num = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newCardNum);
            pstmt.setString(2, newPin);
            pstmt.setString(3, oldCardNum);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "<html><font color='green'>✅ Success!</font></html>");
            } else {
                JOptionPane.showMessageDialog(null, "<html><font color='red'>❌ Not found.</font></html>");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    private String getExistingPin(String cardNum) {
        String pin = "";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement("SELECT card_pin FROM card WHERE card_num = ?")) {

            pstmt.setString(1, cardNum);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                pin = rs.getString("card_pin");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }

        return pin;
    }

    public static void main(String[] args) {
        CardManager manager = new CardManager();

        String[] options = {"Name", "Card Pin"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "EDIT CARD DETAILS\n\nChoose:",
                "Edit Card",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == -1) return;

        String oldCard = JOptionPane.showInputDialog("Current Card Number:");
        if (oldCard == null || oldCard.isEmpty()) return;

        String newCard = null;
        String newPin = null;

        if (choice == 0) {
            newCard = JOptionPane.showInputDialog("Enter New Card Number:");
            if (newCard == null || newCard.isEmpty()) return;
            newPin = manager.getExistingPin(oldCard);
        } else if (choice == 1) {
            newPin = JOptionPane.showInputDialog("Enter New PIN:");
            if (newPin == null || newPin.isEmpty()) return;
            newCard = oldCard;
        }

        manager.updateCardDetails(oldCard, newCard, newPin);
        System.exit(0);
    }
}