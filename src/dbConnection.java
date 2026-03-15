package src;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class dbConnection {
    Account acc;
    Methods m;
    Object[] options = { "OK", "CANCEL" };
    public dbConnection(Account acc, Methods m) {
        this.acc = acc;
        this.m = m;    }
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

    public boolean checkExpiry(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return true;
        }

        try {
            st = conn.prepareStatement("SELECT expiry_date FROM card WHERE card_num = ?");
            st.setString(1, cardNum);
            rs = st.executeQuery();

            if (rs.next()) {
                Timestamp expiryTS = rs.getTimestamp("expiry_date");
                LocalDateTime expiryDate = expiryTS.toLocalDateTime();
                LocalDateTime currentDate = LocalDateTime.now();

                if (currentDate.isAfter(expiryDate)) {
                    int choice = JOptionPane.showConfirmDialog(null,
                            "Your card has been expired. Do you want to renew your card?", "Choose one",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        System.out.println("\nRenewing your card.");
                        renewCard(cardNum);
                    } else if (choice == JOptionPane.NO_OPTION) {
                        st = conn.prepareStatement("DELETE FROM cardholder WHERE card_num = ?");
                        st.setString(1, cardNum);
                        st.executeUpdate();
                        System.out.println("Your card has been deleted");
                        return true;
                    } else {
                        return false;
                    }
                    
                } else {
                        JOptionPane.showOptionDialog(null, "Your card is valid." + "\nClick OK to continue",
                            "SUCCESS",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return false;
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
        return false;
    }

    public boolean checkUser(String cardNum, String cardPin) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        }

        try {
            st = conn.prepareStatement("SELECT * FROM cardholder WHERE card_num = ? AND card_pin = ?");
            st.setString(1, cardNum);
            st.setString(2, cardPin);
            rs = st.executeQuery();

            if (rs.next()) {
                JOptionPane.showOptionDialog(null, "Your account is existing." + "\nClick OK to continue",
                        "SUCCESS",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return true;
            } else {
                JOptionPane.showOptionDialog(null, "Invalid card. Please check you card number or card pin." + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void renewCard(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        try {
            st = conn.prepareStatement("UPDATE card SET expiry_date = ? WHERE card_num = ?");
            LocalDateTime newExpiryDate = LocalDateTime.now().plusYears(1);
            st.setTimestamp(1, Timestamp.valueOf(newExpiryDate));
            st.setString(2, cardNum);
            st.executeUpdate();
            System.out.println("Your card has been renewed. Your new expiry date is: " + newExpiryDate.getMonth() + " "
                    + newExpiryDate.getDayOfMonth() + ", " + newExpiryDate.getYear());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // create new user
    public void register() {
        Scanner sc = new Scanner(System.in);
        Connection conn = getConnection();
        PreparedStatement st = null;

        System.out.print("Enter your first name: ");
        String firstName = sc.nextLine();
        acc.setFirstName(firstName);
        System.out.print("Enter your last name: ");
        String lastName = sc.nextLine();
        acc.setLastName(lastName);
        String cardNum = Methods.generateCardNum();
        acc.setCardNum(cardNum);
        JOptionPane.showOptionDialog(null, "Please save your card number for future uses." + "\nClick OK to continue",
                "SUCCESS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        while (true) {
            System.out.print("\nCreate a 4 digit card pin: ");
            String cardPin = sc.nextLine();
            if (cardPin.matches("\\d{4}")) {
                acc.setCardPin(cardPin);
                System.out.println("Valid pin. Please remember your pin.");
                if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
                    return;
                }
                try {
                    st = conn.prepareStatement(
                            "INSERT INTO cardholder (First_name, Last_name, card_num, card_pin) VALUES (?, ?, ?, ?)");
                    st.setString(1, firstName);
                    st.setString(2, lastName);
                    st.setString(3, cardNum);
                    st.setString(4, cardPin);
                    st.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (st != null)
                            st.close();
                        if (conn != null)
                            conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                expiryDate(cardNum, cardPin);
                break;
            } else {
                JOptionPane.showOptionDialog(null, "Invalid pin. Please enter a 4 digit number." + "\nClick OK to continue",
                        "WARNING",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
            }

        }
        
    }
    
    public void expiryDate(String cardNum, String cardPin) {
        Connection conn = getConnection();
        PreparedStatement st = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }
        LocalDateTime date = LocalDateTime.now(); // get the time and date when the user inputted smth
        LocalDateTime expiryDate = date.plusYears(1); // add 1 yr for expiry date
        acc.setExpiryDate(expiryDate);
        System.out.println("Your expiry date is: " + expiryDate.getMonth() + " " + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear());

        try {
            st = conn.prepareStatement("INSERT INTO card (card_num, card_pin, expiry_date) VALUES (?, ?, ?)");
            st.setString(1, cardNum);
            st.setString(2, cardPin);
            st.setTimestamp(3, Timestamp.valueOf(expiryDate));
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null)
                    st.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean login() throws IOException {
        boolean loginSucess = false;
        while (!loginSucess) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter you card number: ");
            String cardNum = sc.nextLine();
            System.out.print("Enter your card pin: ");
            String cardPin = sc.nextLine();

            acc.setCardNum(cardNum);
            acc.setCardPin(cardPin);
            // boolean validCardNum = m.cardNumChecker(cardNum);
            // if (!validCardNum) {
            //     continue;
            // }
            boolean existing = checkUser(cardNum, cardPin);
            if (!existing) {
                continue;
            }
            boolean expired = checkExpiry(cardNum);
            if (expired) {
                JOptionPane.showMessageDialog(
                        null,
                        "Your card is expired! Returning to main menu.",
                        "Card Expired",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            LoginDetailsSetter(cardNum);
            return true;
        }
        return true;
    }

    //setter for login
    public void LoginDetailsSetter(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }
        try{
            st = conn.prepareStatement("SELECT First_name, Last_name, expiry_date FROM cardholder INNER JOIN card ON cardholder.card_num = card.card_num WHERE cardholder.card_num = ?");
            st.setString(1,cardNum);
            rs = st.executeQuery();
            
            if (rs.next()) {
                acc.setFirstName(rs.getString("First_name"));
                acc.setLastName(rs.getString("Last_name"));
                Timestamp expiryDate = rs.getTimestamp("expiry_date");
                acc.setExpiryDate(expiryDate.toLocalDateTime());
            } else {
                JOptionPane.showOptionDialog(null, "Incorrect card number. Please try again." + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try{
                if(rs != null)
                    rs.close();
                if(st != null)
                    st.close();
                if(conn != null);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
