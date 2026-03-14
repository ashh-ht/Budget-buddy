package src;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

public class dbConnection {
    public static Connection getConnection() {
        String url = "jdbc:mysql://localhost:3306/budgetbuddyproject";
        String username = "root";
        String password = "budgetbuddy-comprog";

        try {
            return DriverManager.getConnection(url, username, password);

        } catch (SQLException e) {
            System.out.println("Connection failed");
            e.printStackTrace();
            return null;
        }
    }

    public static void checkExpiry(int cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            st = conn.prepareStatement("SELECT expiry_date FROM card WHERE card_num = ?");
            st.setInt(1, cardNum);
            rs = st.executeQuery();

            if (rs.next()) {
                Timestamp expiryTS = rs.getTimestamp("expiry_date");
                LocalDateTime expiryDate = expiryTS.toLocalDateTime();
                LocalDateTime currentDate = LocalDateTime.now();

                if (currentDate.isAfter(expiryDate)) {
                    System.out.println("Your card is expired. Please generate a new card.");
                    renewCard(cardNum);
                } else {
                    System.out.println("Your card is valid.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void checkUser(int cardNum, int cardPin) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            st = conn.prepareStatement("SELECT * FROM card WHERE card_num = ? AND card_pin = ?");
            st.setInt(1, cardNum);
            st.setInt(2, cardPin);
            rs = st.executeQuery();

            if (rs.next()) {
                System.out.println("Login successful!");
                //proceed to main menu
            } else {
                System.out.println("Invalid card number or pin. Please try again.");
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void renewCard(int cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            System.out.println("Failed to connect to the database.");
            return;
        }

        try {
            st = conn.prepareStatement("UPDATE card holder SET expiry_date = ? WHERE card_num = ?");
            LocalDateTime newExpiryDate = LocalDateTime.now().plusYears(1);
            st.setTimestamp(1, Timestamp.valueOf(newExpiryDate));
            st.setInt(2, cardNum);
            st.executeUpdate();
            System.out.println("Your card has been renewed. Your new expiry date is: " + newExpiryDate.getMonth() + " "
                    + newExpiryDate.getDayOfMonth() + ", " + newExpiryDate.getYear());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //create new user
    public void register() {
        //add the inserting the values into the database 
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter your first name: ");
        String firstName = sc.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = sc.nextLine();
        Methods.generateCardNum();
        int cardPin;
        while (true) {
            System.out.print("\nCreate a 4 digit card pin: ");
            String pin = sc.nextLine();
            if (pin.matches("\\d{4}")) {
                cardPin = Integer.parseInt(pin);
                sc.close();
                System.out.println("Valid pin. Please remember your pin.");
                break;
            } else {
                System.out.println("Invalid pin. Please create a 4 digit pin.");
            }
        }
    }

}
