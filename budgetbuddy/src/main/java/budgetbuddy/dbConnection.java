package Budgetbuddy;

import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.*;
import javax.swing.JOptionPane;

import com.mysql.cj.x.protobuf.MysqlxPrepare.Prepare;

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

    // format user input
    public String formatInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1))
                    .append(" ");
        }

        return result.toString().trim();
    }

    // get cardID
    public void setCardId(String cardNum) {
        try (Connection conn = getConnection();
                PreparedStatement st = conn.prepareStatement("SELECT id FROM card WHERE card_num = ?")) {
            st.setString(1, cardNum);
            ResultSet rs = st.executeQuery();
            int cardId = -1;
            if (rs.next()) {
                cardId = rs.getInt("id");
                acc.setCardId(cardId);
            } else {
                JOptionPane.showOptionDialog(null,
                        "<html><font color='red'>Card not found! Please try again.</font></html>"
                                + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean checkExpiry(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
                        "<html><font color='red'>Invalid card.</font></html>"
                                + "\nPlease check your card number or card pin." + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
        firstName = formatInput(firstName);
        acc.setFirstName(firstName);
        System.out.print("Enter your last name: ");
        String lastName = sc.nextLine();
        lastName = formatInput(lastName);
        acc.setLastName(lastName);
        String cardNum = Methods.generateCardNum();
        acc.setCardNum(cardNum);
        auth.Login(cardNum);
        JOptionPane.showOptionDialog(null,
                "<html>" + "Please save your card number for future uses: <br>" + "<font color='green'>" + cardNum
                        + "<br></font></html>" + "\nClick OK to continue",
                "SUCCESS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);
        while (true) {
            System.out.print("\nCreate a 4 digit card pin: ");
            String cardPin = sc.nextLine();
            if (cardPin.matches("\\d{4}")) {
                acc.setCardPin(cardPin);
                acc.setHash(Authentication.hashPin(cardPin));
                String hash = acc.getHash();
                JOptionPane.showOptionDialog(null,
                        "Valid pin. Please remember your pin." + "\nClick OK to continue",
                        "SUCCESS",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if (conn == null) {
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Failed to connect to database.</font></html>"
                                    + "\nClick OK to continue",
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
                    st.setString(5, hash);
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
                setCardId(cardNum);
                break;
            } else {
                JOptionPane.showOptionDialog(null,
                        "<html><font color='red'>Invalid pin.</font></html>" + "\nPlease enter a 4 digit number."
                                + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
                String hash = rs.getString("hash");
                acc.setHash(hash);
            } else {
                JOptionPane.showOptionDialog(null,
                        "<html><font color='red'>Invalid pin!</font></html>"
                                + "\nPlease check your input and try again." + "\nClick OK to continue",
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

    public boolean login() {
        boolean loginSuccess = false;
        while (!loginSuccess) {
            Scanner sc = new Scanner(System.in);
            String cardNum;
            while (true) {
                System.out.print("Enter you card number: ");
                cardNum = sc.nextLine();

                if (!cardNum.matches("\\d+")) {
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid input!</font></html>"
                                    + "\nPlease check your input and try again." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    continue;
                }
                boolean validCardNum = cardNumChecker(cardNum);
                if (!validCardNum) {
                    continue;
                }
                break;
            }

            acc.setCardNum(cardNum);
            setCardId(cardNum);
            auth.Login(cardNum);

            while (true) {
                System.out.print("Enter your card pin: ");
                String cardPin = sc.nextLine();
                acc.setCardPin(cardPin);
                getHashedPin(cardNum);
                String hash = acc.getHash();
                boolean pinValid = Authentication.checkPin(cardPin, hash);
                if (!pinValid) {
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid pin!</font></html>"
                                    + "\nPlease check your input and try again." + "\nClick OK to continue",
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
                        "<html><font color='red'>Your card is expired!</font></html>" + "\nReturning to main menu.",
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
                    "<html><font color='red'>Invalid card number!</font></html>"
                            + "\nPlease check your input and try again." + "\nClick OK to continue",
                    "Warning",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
                        "<html><font color='red'>Incorrect card number. Please try again.</font></html>"
                                + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Connection conn = getConnection();
            PreparedStatement st = null;
            ResultSet rs = null;

            if (conn == null) {
                JOptionPane.showOptionDialog(null,
                        "<html><font color='red'>Failed to connect to database.</font></html>"
                                + "\nClick OK to continue",
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
                            "<html><font color='red'>Failed to retrieve balance. Please try again.</font></html>"
                                    + "\nClick OK to continue",
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
        category = formatInput(category);
        System.out.print("Enter estimated category budget for one day: ");
        double budget = sc.nextDouble();
        int cardId = acc.getCardId();
        try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM categories WHERE name = ?")) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                JOptionPane.showOptionDialog(null,
                        "<html><font color = red>Category " + category + " already exists." + "</font></html>",
                        "WARNING",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                        null, options, options[0]);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement st = conn
                .prepareStatement("INSERT INTO categories (name, budget, card_id) VALUES (?, ?, ?)")) {
            st.setString(1, category);
            st.setDouble(2, budget);
            st.setInt(3, cardId);
            st.executeUpdate();

            System.out.println("EWAN KO RIN");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JOptionPane.showOptionDialog(null,
                "<html>Category " + "<font color='#9E00FF'>" + category + "</font>"
                        + " has been added successfully!</html>" + "\nClick OK to continue",
                "Success",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

    }

    // getting boolean value for first time user
    public boolean isFirstTime(String cardNum) {
        Connection conn = getConnection();

        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
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
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Scanner sc = new Scanner(System.in);
            boolean firstTime = isFirstTime(acc.getCardNum());
            while (true) {

                if (firstTime) {
                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~ADD BUDGET CATEGORY~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    System.out.println(
                            "New users are required to add" + Account.Color.PURPLE + " two " + Account.Color.RESET
                                    + "budget categories to help you manage your money better!\n");
                    addCateg(acc.getCardNum());
                    addCateg(acc.getCardNum());
                    setFirstTime(acc.getCardNum());
                    firstTime = false;
                } else {
                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
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

    // financial log
    public boolean FinancialLog() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {

            Scanner sc = new Scanner(System.in);
            int choice;
            while (true) {
                try {

                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~FINANCIAL LOG~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    System.out
                            .println(Account.Color.PURPLE + "1. View Essential Expenses" + Account.Color.RESET);
                    System.out
                            .println(Account.Color.PURPLE + "2. View Non-Essential Expenses"
                                    + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "3. Filter Financial Log" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "4. Add Financial Log" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "5. Return to Main Menu" + Account.Color.RESET);
                    System.out.print("Select from the following options: ");
                    choice = sc.nextInt();
                    sc.nextLine();
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null,
                            "<html><font color = 'red'>Invalid input.</font></html>"
                                    + " Please enter a number from the following."
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                }
            }
            switch (choice) {
                case 1:
                    if (!expensesChecker(Account.status.ESSENTIALS.name())) {
                        JOptionPane.showOptionDialog(null,
                                "<html><font color='red'><br>NO financial record found!</font></html>"
                                        + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                    } else {
                        viewEssential();
                    }
                    break;
                case 2:
                    if (!expensesChecker(Account.status.TREATS.name())) {
                        JOptionPane.showOptionDialog(null,
                                "<html><font color='red'><br>NO financial record found!</font></html>"
                                        + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                    } else {
                        viewTreats();
                    }
                    break;
                case 3:
                    if (!expensesChecker(Account.status.ESSENTIALS.name())
                            && !expensesChecker(Account.status.TREATS.name())) {
                        JOptionPane.showOptionDialog(null,
                                "<html><font color='red'><br>NO financial record found!</font></html>"
                                        + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                    } else {
                        int filterChoice;
                        while (true) {
                            try {
                                System.out.println(
                                        Account.Color.GREEN + "\n==========FILTER FINANCIAL LOG=========="
                                                + Account.Color.RESET);
                                System.out.println(Account.Color.BLUE + "1. Filter by day" + Account.Color.RESET);
                                System.out.println(Account.Color.BLUE + "2. Filter by week" + Account.Color.RESET);
                                System.out.println(Account.Color.BLUE + "3. Filter by month" + Account.Color.RESET);
                                System.out.print("Select from the following options: ");
                                filterChoice = sc.nextInt();
                                break;
                            } catch (InputMismatchException e) {
                                sc.nextLine();
                                JOptionPane.showOptionDialog(null,
                                        "<html><font color = 'red'>Invalid input.</font></html>"
                                                + " Please enter a number from the following."
                                                + "\nClick OK to continue",
                                        "Warning",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);

                            }
                        }

                        switch (filterChoice) {
                            case 1:
                                filterByDay();
                                break;
                            case 2:
                                filterByWeek();
                                break;
                            case 3:
                                filterByMonth();
                                break;
                            default:
                                JOptionPane.showOptionDialog(null,
                                        "<html><font color='red'>Invalid number. Please try again</font></html>"
                                                + "\nClick OK to continue",
                                        "Warning",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);
                                break;
                        }
                    }

                    break;
                case 4:
                    addFinLog();
                    break;
                case 5:
                    return false;
                default:
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid number. Please try again</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }

        }
        return true;
    }

    // check finan log
    public boolean expensesChecker(String status) {
        try (Connection conn = getConnection();
                PreparedStatement ps = conn
                        .prepareStatement("SELECT id FROM expenses WHERE card_id = ? AND status = ?")) {
            ps.setInt(1, acc.getCardId());
            ps.setString(2, status.toLowerCase());
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // add financial log
    public void addFinLog() {
        Connection conn = getConnection();
        Scanner sc = new Scanner(System.in);
        String again = "yes";

        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        System.out.println(Account.Color.GREEN + "==========ADD FINANCIAL LOG==========" + Account.Color.RESET);
        do {
            int choice;
            while (true) {
                try {
                    System.out.println(Account.Color.BLUE + "1. Add in a category (essentials)" + Account.Color.RESET);
                    System.out.println(Account.Color.BLUE + "2. No category(non-essentials)" + Account.Color.RESET);
                    System.out.print("Select from the following options: ");
                    choice = sc.nextInt();
                    sc.nextLine(); // clear buffer
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null,
                            "<html><font color = 'red'>Invalid input.</font></html>"
                                    + " Please enter a number from the following." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                }

            }

            switch (choice) {
                case 1:
                    // add in a category (essentials)
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT name FROM categories WHERE card_id = ?")) {
                        ps.setInt(1, acc.getCardId());
                        ResultSet rs = ps.executeQuery();
                        List<String> categories = new ArrayList<>();
                        while (rs.next()) {
                            categories.add(rs.getString("name"));
                        }
                        if (categories.isEmpty()) {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='red'>No categories found! Please add a category first.</font></html>"
                                            + "\nClick OK to continue",
                                    "Warning",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[0]);
                            return;
                        }
                        System.out.println("CATEGORIES: ");
                        for (int i = 0; i < categories.size(); i++) {
                            System.out.println((i + 1) + ". " + categories.get(i));
                        }
                        System.out.println("Select a category: ");
                        int categoryChoice = sc.nextInt();
                        sc.nextLine(); // clear buffer
                        if (categoryChoice < 1 || categoryChoice > categories.size()) {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='red'>Invalid category choice! Please try again.</font></html>"
                                            + "\nClick OK to continue",
                                    "Warning",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[0]);
                            return;
                        }
                        String selectedCategory = categories.get(categoryChoice - 1);

                        System.out.print("Enter the name of the expense: ");
                        String expenseName = sc.nextLine();
                        expenseName = formatInput(expenseName);

                        System.out.print("Enter the amount of the expense: ");
                        double expenseAmount = sc.nextDouble();
                        sc.nextLine(); // clear buffer
                        try (PreparedStatement st = conn.prepareStatement(
                                "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE c.id = ?")) {
                            st.setInt(1, acc.getCardId());
                            ResultSet balRs = st.executeQuery();
                            double balance = 0;
                            if (balRs.next()) {
                                balance = balRs.getDouble("balance");

                            }
                            if (expenseAmount > balance) {
                                JOptionPane.showOptionDialog(null,
                                        "<html><font color = red> Insufficient balance! Cannot proceed to add. </font><br> Please top-up your card again.</html>",
                                        "Transaction Rejected",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                        null, options, options[0]);
                                return;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                        LocalDate expenseDate = LocalDate.parse(sc.nextLine());
                        while (true) {
                            System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                            String input = sc.nextLine();

                            try {
                                expenseDate = LocalDate.parse(input);
                                if (expenseDate.isAfter(LocalDate.now())) {
                                    JOptionPane.showMessageDialog(null,
                                            "<html><font color = red>Invalid date! </font> Please input valid date.</html>",
                                            "ERROR",
                                            JOptionPane.ERROR_MESSAGE);
                                    continue;
                                }
                                break;
                            } catch (DateTimeParseException e) {
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color = red>Invalid format! </font> Please use YYYY-MM-DD (e.g., 2026-01-31).</html>",
                                        "ERROR",
                                        JOptionPane.ERROR_MESSAGE);
                            }

                        }
                        try (PreparedStatement getCateg = conn
                                .prepareStatement("SELECT id from categories WHERE name = ? AND card_id = ?")) {
                            getCateg.setString(1, selectedCategory);
                            getCateg.setInt(2, acc.getCardId());
                            ResultSet categRs = getCateg.executeQuery();
                            int categID = -1;
                            if (categRs.next()) {
                                categID = categRs.getInt("id");
                            } else {
                                JOptionPane.showOptionDialog(null,
                                        "<html><font color='red'>Category not found! Please try again.</font></html>"
                                                + "\nClick OK to continue",
                                        "Warning",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);
                                return;
                            }
                            try (PreparedStatement logPs = conn.prepareStatement(
                                    "INSERT INTO expenses (category_id, price, task, date, status, card_id) VALUES (?, ?, ?, ?, 'essentials', ?)")) {
                                logPs.setLong(1, categID);
                                logPs.setDouble(2, expenseAmount);
                                logPs.setString(3, expenseName);
                                logPs.setDate(4, java.sql.Date.valueOf(expenseDate));
                                logPs.setInt(5, acc.getCardId());
                                logPs.executeUpdate();
                                JOptionPane.showOptionDialog(null,
                                        "<html><font color='green'>Log added successfully!</font></html>"
                                                + "\nClick OK to continue",
                                        "Success",
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                        null, options, options[0]);
                            }
                            try (PreparedStatement balPs = conn.prepareStatement(
                                    "UPDATE existingmoney em JOIN card c ON em.id = c.existingmoney_id SET em.balance = em.balance - ? WHERE c.id = ?")) {
                                balPs.setDouble(1, expenseAmount);
                                balPs.setInt(2, acc.getCardId());
                                balPs.executeUpdate();
                            }

                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    // no category(non-essentials)
                    System.out.print("Enter the name of the expense: ");
                    String expenseName = sc.nextLine();
                    expenseName = formatInput(expenseName);
                    System.out.print("Enter the amount of the expense: ");
                    double expenseAmount = sc.nextDouble();
                    sc.nextLine(); // clear buffer
                    try (PreparedStatement st = conn.prepareStatement(
                            "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE c.id = ?")) {
                        st.setInt(1, acc.getCardId());
                        ResultSet balRs = st.executeQuery();
                        double balance = 0;
                        if (balRs.next()) {
                            balance = balRs.getDouble("balance");

                        }
                        if (expenseAmount > balance) {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color = red> Insufficient balance! Cannot proceed to add. </font><br> Please top-up your card again.</html>",
                                    "Transaction Rejected",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                    null, options, options[0]);
                            return;
                        }
                        System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                        LocalDate expenseDate = LocalDate.parse(sc.nextLine());
                        while (true) {
                            System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                            String input = sc.nextLine();

                            try {
                                expenseDate = LocalDate.parse(input);
                                if (expenseDate.isAfter(LocalDate.now())) {
                                    JOptionPane.showMessageDialog(null,
                                            "<html><font color = red>Invalid date! </font> Please input valid date.</html>",
                                            "ERROR",
                                            JOptionPane.ERROR_MESSAGE);
                                    continue;
                                }
                                break;
                            } catch (DateTimeParseException e) {
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color = red>Invalid format! </font> Please use YYYY-MM-DD (e.g., 2026-01-31).</html>",
                                        "ERROR",
                                        JOptionPane.ERROR_MESSAGE);
                            }

                        }
                        try (PreparedStatement logPs = conn.prepareStatement(
                                "INSERT INTO expenses (price, task, date, status, card_id) VALUES (?, ?, ?, 'treats', ?)")) {
                            logPs.setDouble(1, expenseAmount);
                            logPs.setString(2, expenseName);
                            logPs.setDate(3, java.sql.Date.valueOf(expenseDate));
                            logPs.setInt(4, acc.getCardId());
                            logPs.executeUpdate();
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='green'>Log added successfully!</font></html>"
                                            + "\nClick OK to continue",
                                    "Success",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, options[0]);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid number. Please try again</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }

            System.out.println("Do you want to add another log? (yes/no)");
            again = sc.nextLine();
        } while (again.equalsIgnoreCase("yes") || again.equalsIgnoreCase("y"));

    }

    // view essential expenses
    public void viewEssential() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }
            List<String> essentials = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT task, price, date FROM expenses WHERE status = 'essentials' AND card_id = ?")) {
                ps.setInt(1, acc.getCardId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String task = rs.getString("task");
                    double price = rs.getDouble("price");
                    java.sql.Date date = rs.getDate("date");
                    essentials
                            .add(String.format("%-10s | PHP %-5.2f | %-10s", task, price, date.toString()));
                }

                if (essentials.isEmpty()) {
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>No essential expenses found!</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return;
                }

                System.out.println(Account.Color.GREEN + "==========ESSENTIAL EXPENSES==========="
                        + Account.Color.RESET);
                for (String expense : essentials) {
                    System.out.println(expense);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    // view non-essential expenses
    public void viewTreats() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }
            List<String> treats = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT task, price, date FROM expenses WHERE status = 'treats' AND card_id = ?")) {
                ps.setInt(1, acc.getCardId());
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String task = rs.getString("task");
                    double price = rs.getDouble("price");
                    java.sql.Date date = rs.getDate("date");
                    treats.add(String.format("%-10s | PHP %-5.2f | %-10s", task, price, date.toString()));
                }

                if (treats.isEmpty()) {
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>No non-essential expenses found!</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    return;
                }

                System.out
                        .println(Account.Color.GREEN + "==========NON-ESSENTIAL EXPENSES==========="
                                + Account.Color.RESET);
                for (String expense : treats) {
                    System.out.println(expense);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    // filter financial log
    public void filterByDay() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        Map<String, List<String>> daily = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT task, price, date, status FROM expenses WHERE card_id = ? ORDER BY date ASC")) {
            ps.setInt(1, acc.getCardId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String task = rs.getString("task");
                double price = rs.getDouble("price");
                LocalDate date = rs.getDate("date").toLocalDate();
                String status = rs.getString("status");
                String logEntry = String.format("%-10s | PHP %-5.2f | %-10s", task, price, status);
                String day = date.getDayOfWeek().toString();
                daily.putIfAbsent(day, new ArrayList<>());
                daily.get(day).add(logEntry);
            }
            System.out.println(Account.Color.YELLOW + "==========DAILY==========" + Account.Color.RESET);
            for (String day : daily.keySet()) {
                System.out.println(Account.Color.CYAN + day + Account.Color.RESET);
                for (String log : daily.get(day)) {
                    System.out.println("    " + log);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void filterByWeek() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        Map<String, Map<Integer, List<String>>> weekly = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT task, price, date, status FROM expenses WHERE card_id = ? ORDER BY date ASC")) {
            ps.setInt(1, acc.getCardId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                String month = date.getMonth().toString();
                int week = date.get(WeekFields.ISO.weekOfMonth());
                String task = rs.getString("task");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                String logEntry = String.format("%-10s | PHP %-5.2f | %-10s", task, price, status);
                weekly.putIfAbsent(month, new LinkedHashMap<>());
                weekly.get(month).putIfAbsent(week, new ArrayList<>());
                weekly.get(month).get(week).add(logEntry);
            }

            System.out.println(Account.Color.YELLOW + "==========WEEKLY==========" + Account.Color.RESET);

            for (String month : weekly.keySet()) {
                System.out.println(Account.Color.CYAN + month + Account.Color.RESET);
                for (Integer week : weekly.get(month).keySet()) {
                    System.out.println(Account.Color.BLUE + "    Week " + week + Account.Color.RESET);
                    for (String log : weekly.get(month).get(week)) {
                        System.out.println("        " + log);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void filterByMonth() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return;
        }

        Map<String, List<String>> monthly = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT task, price, date, status FROM expenses WHERE card_id = ? ORDER BY date ASC")) {
            ps.setInt(1, acc.getCardId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                String month = date.getMonth().toString();
                String task = rs.getString("task");
                double price = rs.getDouble("price");
                String status = rs.getString("status");
                String logEntry = String.format("%-10s | PHP %-5.2f | %-10s", task, price, status);
                monthly.putIfAbsent(month, new ArrayList<>());
                monthly.get(month).add(logEntry);
            }

            System.out.println(Account.Color.YELLOW + "==========MONTHLY==========" + Account.Color.RESET);
            for (String month : monthly.keySet()) {
                System.out.println(Account.Color.CYAN + month + Account.Color.RESET);
                for (String log : monthly.get(month)) {
                    System.out.println("    " + log);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // balance checker
    public boolean balanceChecker() {
        Connection conn = getConnection();
        if (conn == null) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'>Failed to connect to database.</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        }

        try (PreparedStatement bal = conn.prepareStatement(
                "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
            bal.setString(1, acc.getCardNum());
            ResultSet balRs = bal.executeQuery();
            double balance = 0;
            if (balRs.next()) {
                balance = balRs.getDouble("balance");
            }
            if (balance <= 100) {
                JOptionPane.showOptionDialog(null,
                        "<html><font color='red'>Your balance is below PHP 100! Please top up your account.</font></html>"
                                + "\nClick OK to continue",
                        "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                return false;
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // view budget allocation
    public boolean viewBudget() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Scanner sc = new Scanner(System.in);
            int choice;
            while (true) {
                try {
                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~VIEW BUDGET~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "1. Essential" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "2. Non-Essential" + Account.Color.RESET);
                    System.out.println("Select from the following options:");
                    choice = sc.nextInt();
                    sc.nextLine();
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null,
                            "<html><font color = 'red'>Invalid input.</font></html>"
                                    + " Please enter a number from the following." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                }
            }

            switch (choice) {
                case 1:
                    essentialBudget();
                    break;
                case 2:
                    treatsBudget();
                    break;
                default:
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid number. Please try again</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }

        }
        return true;
    }

    public void essentialBudget() {
        if (!expensesChecker(Account.status.ESSENTIALS.name())) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'><br>NO financial record found!</font></html>"
                            + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
        } else {
            List<String> budgetAlloc = new ArrayList<>();
            Connection conn = getConnection();
            try (PreparedStatement bal = conn.prepareStatement(
                    "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                bal.setString(1, acc.getCardNum());
                ResultSet balRs = bal.executeQuery();
                double balance = 0;
                if (balRs.next()) {
                    balance = balRs.getDouble("balance");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT c.name, c.budget, IFNULL(SUM(e.price), 0) AS total_expenses FROM categories c LEFT JOIN expenses e ON c.id = e.category_id AND e.card_id = ? WHERE c.card_id = ? AND e.status = 'essentials' GROUP BY c.id")) {
                    ps.setInt(1, acc.getCardId());
                    ps.setInt(2, acc.getCardId());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        double budget = rs.getDouble("budget");
                        double expenses = rs.getDouble("total_expenses");
                        double percentBudget = 0;
                        double percentBal = 0;
                        if (budget > 0) {
                            percentBudget = (expenses / budget) * 100;
                            if (percentBudget > 100) {
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color = red> Your expenses exceeds your daily budget! </font>",
                                        "Budget Exceeded",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        if (balance > 0) {
                            percentBal = (expenses / balance) * 100;
                            if (percentBal > 100) {
                                JOptionPane.showMessageDialog(null,
                                        "<html><font color = red> Your expenses exceeds your balance! </font>",
                                        "Budget Exceeded",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }

                        budgetAlloc.add(String.format(
                                "%-15s | PHP %-11.2f | PHP %-11.2f | %-16s | %-15s",
                                name, budget, expenses, String.format("%.2f%%", percentBudget),
                                String.format("%.2f%%", percentBal)));
                    }
                    System.out.println(Account.Color.GREEN + "==========ESSENTIAL BUDGET=========="
                            + Account.Color.RESET);
                    System.out.println(
                            "%-15s | %-15s | %-15s | %-15s | %-15s".formatted("Category", "Budget in a day",
                                    "All Expenses", "% used in Budget", "% used in Balance"));
                    for (String alloc : budgetAlloc) {
                        System.out.println(alloc);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void treatsBudget() {
        if (!expensesChecker(Account.status.TREATS.name())) {
            JOptionPane.showOptionDialog(null,
                    "<html><font color='red'><br>NO financial record found!</font></html>"
                            + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
        } else {
            List<String> budgetAlloc = new ArrayList<>();
            Connection conn = getConnection();
            try (PreparedStatement bal = conn.prepareStatement(
                    "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                bal.setString(1, acc.getCardNum());
                ResultSet balRs = bal.executeQuery();
                double balance = 0;
                if (balRs.next()) {
                    balance = balRs.getDouble("balance");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT c.name, c.budget, IFNULL(SUM(e.price), 0) AS total_expenses FROM categories c LEFT JOIN expenses e ON c.id = e.category_id AND e.card_id = ? WHERE c.card_id = ? AND e.status = 'treats' GROUP BY c.id")) {
                    ps.setInt(1, acc.getCardId());
                    ps.setInt(2, acc.getCardId());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        String name = rs.getString("name");
                        double budget = rs.getDouble("budget");
                        double expenses = rs.getDouble("total_expenses");
                        double percentBudget = 0;
                        double percentBal = 0;
                        if (expenses > 0) {
                            percentBudget = (expenses / budget) * 100;
                            percentBal = (expenses / balance) * 100;
                        }
                        budgetAlloc.add(String.format("%-15s | PHP %-11.2f | PHP %-11.2f | %-16s | %-15s",
                                name, budget,
                                String.format("%.2f%%", percentBudget),
                                String.format("%.2f%%", percentBal)));
                    }
                    System.out.println(
                            Account.Color.GREEN + "==========NON-ESSENTIAL BUDGET=========="
                                    + Account.Color.RESET);
                    System.out.println(
                            "%-15s | %-15s | %-15s | %-15s | %-15s".formatted("Category", "Budget in a day",
                                    "All Expenses", "% used in Budget", "% used in Balance"));
                    for (String alloc : budgetAlloc) {
                        System.out.println(alloc);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // edit account details
    public boolean editAccDetails() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Scanner sc = new Scanner(System.in);
            int choice;
            while (true) {
                try {
                    System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                            + "\n~~~~~~~~~~~~~~~~~~~~EDIT ACCOUNT DETAILS~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "1. Edit Name" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "2. Edit PIN" + Account.Color.RESET);
                    System.out.print("Pick from the following: ");
                    choice = sc.nextInt();
                    sc.nextLine();// clears buffer
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null,
                            "<html><font color = 'red'>Invalid input.</font></html>"
                                    + " Please enter a number from the following." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);

                }
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter your new first name: ");
                    String newFirstName = sc.nextLine();
                    newFirstName = formatInput(newFirstName);
                    System.out.print("Enter your new last name: ");
                    String newLastName = sc.nextLine();
                    newLastName = formatInput(newLastName);

                    try (Connection conn = getConnection();
                            PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE cardholder SET first_name = ?, last_name = ? WHERE card_num = ?")) {
                        ps.setString(1, newFirstName);
                        ps.setString(2, newLastName);
                        ps.setString(3, acc.getCardNum());
                        int updated = ps.executeUpdate();
                        if (updated > 0) {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='green'>Name updated successfully!</font></html>"
                                            + "\nClick OK to continue",
                                    "Success",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                    null, options, options[0]);
                            acc.setFirstName(newFirstName);
                            acc.setLastName(newLastName);
                        } else {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='red'>Failed to update name. Please try again.</font></html>"
                                            + "\nClick OK to continue",
                                    "Error",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                    null, options, options[0]);
                            break;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    while (true) {
                        System.out.print("\nCreate a 4 digit card pin: ");
                        String cardPin = sc.nextLine();
                        if (cardPin.matches("\\d{4}")) {
                            acc.setCardPin(cardPin);
                            acc.setHash(Authentication.hashPin(cardPin));
                            String hash = acc.getHash();
                            JOptionPane.showOptionDialog(null,
                                    "Valid pin. Please remember your pin." + "\nClick OK to continue",
                                    "SUCCESS",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[0]);

                            try (Connection conn = getConnection();
                                    PreparedStatement ps = conn.prepareStatement(
                                            "UPDATE cardholder SET card_pin = ?, hash = ? WHERE card_num = ?")) {
                                ps.setString(1, cardPin);
                                ps.setString(2, hash);
                                ps.setString(3, acc.getCardNum());
                                int updated = ps.executeUpdate();
                                if (updated > 0) {
                                    JOptionPane.showOptionDialog(null,
                                            "<html><font color='green'>PIN updated successfully!</font></html>"
                                                    + "\nClick OK to continue",
                                            "Success",
                                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                            null, options, options[0]);
                                } else {
                                    JOptionPane.showOptionDialog(null,
                                            "<html><font color='red'>Failed to update PIN. Please try again.</font></html>"
                                                    + "\nClick OK to continue",
                                            "Error",
                                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                            null, options, options[0]);
                                    break;
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                        } else {
                            JOptionPane.showOptionDialog(null,
                                    "<html><font color='red'>Invalid pin. Please enter a 4 digit number.</font></html>"
                                            + "\nClick OK to continue",
                                    "Warning",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[0]);
                            continue;
                        }
                        break;
                    }
                    break;
                default:
                    JOptionPane.showOptionDialog(null,
                            "<html><font color='red'>Invalid number. Please try again</font></html>"
                                    + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;

            }
        }
        return true;
    }

    // deposit cash
    public boolean depositCash() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            JOptionPane.showOptionDialog(null,
                    "<html>" + "Your session has expired. Please login again."
                            + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        } else {
            Scanner sc = new Scanner(System.in);
            System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                    + "\n~~~~~~~~~~~~~~~~~~~~DEPOSIT CASH~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
            System.out.print("Enter the amount you want to deposit: ");
            int amount = sc.nextInt();
            sc.nextLine(); // clear buffer
            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE existingmoney em JOIN card c ON em.id = c.existingmoney_id SET em.balance = em.balance + ?, em.deposit = ? WHERE c.card_num = ?")) {
                ps.setInt(1, amount);
                ps.setInt(2, amount);
                ps.setString(3, acc.getCardNum());
                ps.executeUpdate();
                JOptionPane.showOptionDialog(null,
                        "<html><font color='green'>Amount PHP " + amount + " deposited successfully!</font></html>"
                                + "\nClick OK to continue",
                        "Success",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options, options[0]);

                try (PreparedStatement bal = conn.prepareStatement(
                        "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                    bal.setString(1, acc.getCardNum());
                    ResultSet balRs = bal.executeQuery();
                    double balance = 0;
                    if (balRs.next()) {
                        balance = balRs.getDouble("balance");
                    }

                    if (balance < 100) {
                        JOptionPane.showOptionDialog(null,
                                "<html><font color='red'>Your balance is still below PHP 100! Please top up your account.</font></html>"
                                        + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
