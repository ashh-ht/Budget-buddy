import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;

public class CardManager {

    String url = "jdbc:mysql://localhost:3306/budgetbuddyproject";
    String user = "root";
    String password = ""; 

    public void updateCardPin(String cardNum, String newPin) {
        String sql = "UPDATE card SET card_pin = ? WHERE card_num = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPin);
            pstmt.setString(2, cardNum);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "✅ Card PIN updated!");
            } else {
                JOptionPane.showMessageDialog(null, "❌ Card Number not found.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // FIXED: Changed CardDatabaseEditor to CardManager
        CardManager manager = new CardManager();
        
        String cardNum = JOptionPane.showInputDialog("Enter Card Number:");
        String newPin = JOptionPane.showInputDialog("Enter New PIN:");

        manager.updateCardPin(cardNum, newPin);
    }
}