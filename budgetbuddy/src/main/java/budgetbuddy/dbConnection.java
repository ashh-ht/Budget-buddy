package Budgetbuddy;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class dbConnection {
    Account acc;
    Authentication auth;

    public dbConnection(Account acc, Authentication auth) {
        this.acc = acc;
        this.auth = auth;
    }

    Object[] options = { "OK", "CANCEL" };

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

    // formatting the account details
    public void AccountDetails() {
        String firstName = acc.getFirstName();
        String lastName = acc.getLastName();
        String cardNum = acc.getCardNum();
        LocalDateTime expiryDate = acc.getExpiryDate();
        String name = firstName + " " + lastName;
        System.out.println(Account.Color.GREEN + Account.Color.BOLD + "\n\n==========ACCOUNT DETAILS=========="
                + Account.Color.RESET);
        System.out.print(Account.Color.BLUE + "\nACCOUNT NAME: " + Account.Color.RESET + name);
        System.out.println(Account.Color.BLUE + "\nCARD NUMBER: " + Account.Color.RESET + cardNum);
        System.out.println(Account.Color.BLUE + "CARD EXPIRATION DATE: " + Account.Color.RESET + expiryDate.getMonth()
                + " " + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear());
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
                    JOptionPane.showOptionDialog(null, "Your card is not expired yet." + "\nClick OK to continue",
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
                JOptionPane.showOptionDialog(null,
                        "Invalid card. Please check you card number or card pin." + "\nClick OK to continue",
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
        auth.Login(cardNum);
        JOptionPane.showOptionDialog(null,
                "Please save your card number for future uses: \n" + cardNum + "\nClick OK to continue",
                "SUCCESS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        while (true) {
            System.out.print("\nCreate a 4 digit card pin: ");
            String cardPin = sc.nextLine();
            if (cardPin.matches("\\d{4}")) {
                acc.setCardPin(cardPin);
                acc.setHash(Authentication.hashPin(cardPin));
                String hashedPin = acc.getHash();
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
                            "INSERT INTO cardholder (First_name, Last_name, card_num, card_pin, hash) VALUES (?, ?, ?, ?, ?)");
                    st.setString(1, firstName);
                    st.setString(2, lastName);
                    st.setString(3, cardNum);
                    st.setString(4, cardPin);
                    st.setString(5, hashedPin);
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
                createExistingMoney(cardNum);
                break;
            } else {
                JOptionPane.showOptionDialog(null,
                        "Invalid pin. Please enter a 4 digit number." + "\nClick OK to continue",
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

    public void getHashedPin(String cardNum) {
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
            st = conn.prepareStatement("SELECT hash FROM cardholder WHERE card_num = ?");
            st.setString(1, cardNum);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                String hashedPin = rs.getString("hash");
                acc.setHash(hashedPin);
            } else {
                JOptionPane.showOptionDialog(null,
                        "Invalid pin. Please check your input and try again." + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return;
            }
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
        boolean loginSuccess = false;
        while (!loginSuccess) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter you card number: ");
            String cardNum = sc.nextLine();
            acc.setCardNum(cardNum);
            auth.Login(cardNum);
            boolean validCardNum = cardNumChecker(cardNum);
            if (!validCardNum) {
                continue;
            }
            while (true) {
                System.out.print("Enter your card pin: ");
                String cardPin = sc.nextLine();
                acc.setCardPin(cardPin);
                getHashedPin(cardNum);
                String hashedPin = acc.getHash();
                boolean pinValid = Authentication.checkPin(cardPin, hashedPin);
                if (!pinValid) {
                    JOptionPane.showOptionDialog(null,
                            "Invalid pin. Please check your input and try again." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    continue;
                }
                break;
            }
            String cardPin = acc.getCardPin();
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

    // check if the card is valid or not
    public boolean cardNumChecker(String cardNum) {
        long lastDigit = Long.parseLong(cardNum) % 10; // get the last number of the card num
        long secondLastDigit = (Long.parseLong(cardNum) / 10) % 10; // get the second last number
        long sum = lastDigit + secondLastDigit; // add the last and second last number

        if (sum == 10) {
            JOptionPane.showMessageDialog(null, "Valid Card Number!");
            return true;
        } else {
            JOptionPane.showOptionDialog(null,
                    "Invalid card number. Please check your input and try again" + "\nClick OK to continue", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        }
    }

    // setter for login
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
        try {
            st = conn.prepareStatement(
                    "SELECT First_name, Last_name, expiry_date FROM cardholder INNER JOIN card ON cardholder.card_num = card.card_num WHERE cardholder.card_num = ?");
            st.setString(1, cardNum);
            rs = st.executeQuery();

            if (rs.next()) {
                acc.setFirstName(rs.getString("First_name"));
                acc.setLastName(rs.getString("Last_name"));
                Timestamp expiryDate = rs.getTimestamp("expiry_date");
                acc.setExpiryDate(expiryDate.toLocalDateTime());
            } else {
                JOptionPane.showOptionDialog(null,
                        "Incorrect card number. Please try again." + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (st != null)
                    st.close();
                if (conn != null)
                    ;
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // creating data in existingmoney table
    public void createExistingMoney(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement stMoney = null;
        PreparedStatement stCard = null;
        ResultSet rs = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        try {
            conn.setAutoCommit(false);

            // creating column data in existingmoney table
            stMoney = conn.prepareStatement("INSERT INTO existingmoney (balance, deposit) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stMoney.setDouble(1, 0);
            stMoney.setDouble(2, 0);
            stMoney.executeUpdate();

            rs = stMoney.getGeneratedKeys();
            int moneyID = 0;
            if (rs.next()) {
                moneyID = rs.getInt(1);
            }

            // updating the card table
            stCard = conn.prepareStatement("UPDATE card SET existingmoney_id = ? WHERE card_num = ?");
            stCard.setInt(1, moneyID);
            stCard.setString(2, cardNum);
            stCard.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // undo commit if may error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (rs != null)
                    rs.close();
                if (stMoney != null)
                    stMoney.close();
                if (stCard != null)
                    stCard.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // view balance
    public boolean viewBalance() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "Your session has expired. Please login again. LOGGING OUT..." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
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
                st = conn.prepareStatement(
                        "SELECT em.balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?");
                st.setString(1, acc.getCardNum());
                rs = st.executeQuery();
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~BALANCE~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    AccountDetails();
                    System.out.printf("BALANCE: PHP %.2f%n", balance);
                } else {
                    JOptionPane.showOptionDialog(null,
                            "Failed to retrieve balance. Please try again." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return false;
                }
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
        return true;
    }

    // add category
    public void addCateg(String cardNum) {
        Scanner sc = new Scanner(System.in);
        Connection conn = getConnection();

        System.out.print("Enter category name(eg. Food, Allowance, etc.): ");
        String category = sc.nextLine();
        System.out.print("Enter estimated category budget for one day: ");
        double budget = sc.nextDouble();

        try (PreparedStatement st1 = conn.prepareStatement("SELECT id FROM card WHERE card_num = ?")) {
            st1.setString(1, cardNum); // getting card id first
            ResultSet rs = st1.executeQuery();
            int cardId = -1;
            if (rs.next()) {
                cardId = rs.getInt("id");
            } else {
                JOptionPane.showOptionDialog(null,
                        "Card not found! Please try again." + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return;
            }

            PreparedStatement st2 = conn
                    .prepareStatement("INSERT INTO categories (name, budget, card_id) VALUES (?, ?, ?)");
            st2.setString(1, category);
            st2.setDouble(2, budget);
            st2.setLong(3, cardId);
            st2.executeUpdate();

            JOptionPane.showOptionDialog(null,
                    "Category " + category + " has been added successfully!" + "\nClick OK to continue",
                    "Success",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // getting boolean value for first time user
    public boolean isFirstTime(String cardNum) {
        Connection conn = getConnection();

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        }

        try (PreparedStatement st = conn.prepareStatement("SELECT first_time FROM cardholder WHERE card_num = ?")) {
            st.setString(1, cardNum);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("first_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public void setFirstTime(String cardNum) {
        Connection conn = getConnection();

        if (conn == null) {
            JOptionPane.showOptionDialog(null, "Failed to connect to database." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        try (PreparedStatement st = conn.prepareStatement("UPDATE cardholder SET first_time = 0 WHERE card_num = ?")) {
            st.setString(1, cardNum);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // add budget category flow
    public boolean addCategFlow() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "Your session has expired. Please login again. LOGGING OUT..." + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Scanner sc = new Scanner(System.in);
            boolean firstTime = isFirstTime(acc.getCardNum());
            while (true) {

                if (firstTime) {
                    System.out.println(Account.Color.GREEN + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~ADD BUDGET CATEGORY~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    System.out.println(
                            "New users are required to add two budget categories to help you manage your money better!\n");
                    addCateg(acc.getCardNum());
                    addCateg(acc.getCardNum());
                    setFirstTime(acc.getCardNum());
                    firstTime = false;
                } else {
                    System.out.println(Account.Color.GREEN + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~ADD BUDGET CATEGORY~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    addCateg(acc.getCardNum());
                }
                System.out.println("Do you want to add another category? (yes/no)");
                String choice = sc.nextLine();

                if (choice.equalsIgnoreCase("no") || choice.equalsIgnoreCase("n")) {
                    JOptionPane.showOptionDialog(null,
                            "Returning to main menu." + "\nClick OK to continue",
                            "Information",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                            null, options, options[0]);
                    break;
                }
            }

        }
        return true;
    }
}
