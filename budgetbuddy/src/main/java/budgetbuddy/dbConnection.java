package Budgetbuddy;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.*;
import javax.swing.JOptionPane;

public class dbConnection {
    Account acc;
    Authentication auth;

    public dbConnection(Account acc, Authentication auth) {
        this.acc = acc;
        this.auth = auth;
    }

    Object[] options = { "OK", "CANCEL" };
    Scanner sc = new Scanner(System.in);
    String title;
    String message;

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
        if (input == null || input.isBlank()) {
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
                message = "<font color='red'>Card not found! Please try again.</font>"
                        + "<br>Click OK to continue";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
                        message = "Renewing your card.";
                        title = "RENEW";
                        Methods.showMessage(title, message);
                        renewCard(cardNum);
                    } else if (choice == JOptionPane.NO_OPTION) {
                        try {
                            conn.setAutoCommit(false);

                            PreparedStatement getId = conn
                                    .prepareStatement("SELECT existingmoney_id FROM card WHERE card_num = ?");
                            getId.setString(1, cardNum);
                            ResultSet rsId = getId.executeQuery();

                            if (rs.next()) {
                                int moneyId = rsId.getInt("existingmoney_id");

                                PreparedStatement stMoney = conn
                                        .prepareStatement("DELETE FROM existingmoney WHERE id = ?");
                                stMoney.setInt(1, moneyId);
                                stMoney.executeUpdate();
                            }
                            st = conn.prepareStatement("DELETE FROM cardholder WHERE card_num = ?");
                            st.setString(1, cardNum);
                            st.executeUpdate();

                            conn.commit();

                        } catch (SQLException e) {
                            conn.rollback();
                        }

                        message = "Your card has been deleted";
                        title = "DELETED!";
                        Methods.showMessage(title, message);
                        return true;
                    } else {
                        return false;
                    }

                } else {
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

    public boolean checkUser(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        }

        try {
            st = conn.prepareStatement("SELECT * FROM cardholder WHERE card_num = ?");
            st.setString(1, cardNum);
            rs = st.executeQuery();

            if (rs.next()) {
                return true;
            } else {
                message = "<font color='red'>Invalid card.</font>"
                        + "<br>Please check your card number if it is correct." + "<br>Click OK to continue";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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

        Connection conn = getConnection();
        PreparedStatement st = null;
        String firstName;
        String lastName;
        while (true) {
            System.out.print("Enter your first name: ");
            firstName = sc.nextLine();
            firstName = formatInput(firstName);
            if (!firstName.isBlank()) {
                acc.setFirstName(firstName);
                break;
            }
        }
        while (true) {
            System.out.print("Enter your last name: ");
            lastName = sc.nextLine();
            lastName = formatInput(lastName);
            if (!lastName.isBlank()) {
                acc.setLastName(lastName);
                break;
            }
        }

        String cardNum = Methods.generateCardNum();
        acc.setCardNum(cardNum);
        auth.Login(cardNum);
        message = "" + "Please save your card number for future uses: <br>" + "<font color='green'>" + cardNum
                + "<br></font>" + "<br>Click OK to continue.";
        title = "SUCCESS!";
        Methods.showMessage(title, message);
        while (true) {
            System.out.print("\nCreate a 4 digit card pin: ");
            String cardPin = sc.nextLine();
            if (cardPin.matches("\\d{4}")) {
                acc.setHash(Authentication.hashPin(cardPin));
                String hash = acc.getHash();
                message = "Valid pin. Please remember your pin." + "<br>Click OK to continue.";
                title = "SUCCESS!";
                Methods.showMessage(title, message);

                if (conn == null) {
                    message = "<font color='red'>Failed to connect to database.</font>"
                            + "<br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    return;
                }
                try {
                    st = conn.prepareStatement(
                            "INSERT INTO cardholder (First_name, Last_name, card_num, hash) VALUES (?, ?, ?, ?)");
                    st.setString(1, firstName);
                    st.setString(2, lastName);
                    st.setString(3, cardNum);
                    st.setString(4, hash);
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

                expiryDate(cardNum);
                createExistingMoney(cardNum);
                setCardId(cardNum);
                break;
            } else {
                message = "<font color='red'>Invalid pin.</font>" + "<br>Please enter a 4 digit number."
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
            }

        }

    }

    public void expiryDate(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;

        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return;
        }
        LocalDateTime date = LocalDateTime.now(); // get the time and date when the user inputted smth
        LocalDateTime expiryDate = date.plusYears(1); // add 1 yr for expiry date
        acc.setExpiryDate(expiryDate);
        message = "Your card will expire in: <font color = #029a09>" + expiryDate.getMonth() + " "
                + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear() + "</font>";
        title = "CARD EXPIRY DATE";
        Methods.showMessage(title, message);
        try {
            st = conn.prepareStatement("INSERT INTO card (card_num, expiry_date) VALUES (?, ?)");
            st.setString(1, cardNum);
            st.setTimestamp(2, Timestamp.valueOf(expiryDate));
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
                message = "<font color='red'>Invalid pin!</font>"
                        + "<br>Please check your input and try again." + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
        // will return false if one credential of user is wrong
        while (!loginSuccess) {
            String cardNum;
            boolean success = false;
            for (int i = 0; i < 3; i++) {
                System.out.print("Enter you card number: ");
                cardNum = sc.nextLine();

                if (!cardNum.matches("\\d+")) { // \\d+ is checking if there is only digit in the input
                    i--;
                    message = "<font color='red'>Invalid input!</font>"
                            + "<br>Please check your input and try again." + "<br>Click OK to continue";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    continue;
                }
                boolean validCardNum = cardNumChecker(cardNum);
                if (!validCardNum) {
                    int attemptsLeft = 2 - i;
                    if (attemptsLeft == 0) {
                        break;
                    }
                    message = "<font color = red>Wrong card number!</font> Only " + attemptsLeft +
                            " attempts left. <br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    continue;
                }
                boolean existing = checkUser(cardNum);
                if (existing) {
                    acc.setCardNum(cardNum);
                    setCardId(cardNum);
                    auth.Login(cardNum);
                    success = true;
                    break;
                } else {
                    int attemptsLeft = 2 - i;
                    if (attemptsLeft == 0) {
                        break;
                    }
                    message = "<font color = red>Wrong card number!</font> Only " + attemptsLeft +
                            " attempts left. <br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                }
            }
            if (!success) {
                message = "Too many failed attempts. EXITING..." +
                        "<br>Click OK to continue";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
                return false;
            }
            cardNum = acc.getCardNum();

            for (int i = 0; i < 3; i++) {
                success = false;
                System.out.print("Enter your card pin: ");
                String cardPin = sc.nextLine();

                if (!cardPin.matches("\\d+")) { // \\d+ is checking if there is only digit in the input
                    i--;
                    message = "<font color='red'>Invalid input!</font>"
                            + "<br>Please check your input and try again." + "<br>Click OK to continue";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    continue;
                }
                getHashedPin(cardNum);
                String hash = acc.getHash();
                boolean pinValid = Authentication.checkPin(cardPin, hash);
                if (!pinValid) {
                    int attemptLeft = 2 - i;
                    if (attemptLeft == 0) {
                        break;
                    }
                    message = "<font color = red>Wrong card pin!</font> Only " + attemptLeft +
                            " attempts left. <br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                } else {
                    success = true;
                    break;
                }
            }
            if (!success) {
                message = "Too many failed attempts. EXITING..." +
                        "<br>Click OK to continue";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
                return false;
            }

            boolean expired = checkExpiry(cardNum);
            if (expired) {
                message = "<font color='red'>Your card is expired!</font>" + "<br>Returning to main menu.";
                title = "Card Expired";
                Methods.showErrorMessage(title, message);
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
        String noSpace = cardNum.replaceAll("\\s+", ""); // ensures that the input card num has no white spaces

        if (sum == 10 && noSpace.length() == 16) {
            return true;
        } else {
            message = "<font color='red'>Invalid card number!</font>"
                    + "<br>Please check your input and try again." + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        }
    }

    // setter for login
    public void LoginDetailsSetter(String cardNum) {
        Connection conn = getConnection();
        PreparedStatement st = null;
        ResultSet rs = null;

        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
                message = "<font color='red'>Incorrect card number. Please try again.</font>"
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return;
        }

        try {
            conn.setAutoCommit(false); // will not automatically run the query in database

            // creating column data in existingmoney table
            stMoney = conn.prepareStatement("INSERT INTO existingmoney (balance, deposit) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stMoney.setDouble(1, 0);
            stMoney.setDouble(2, 0);
            stMoney.executeUpdate();

            rs = stMoney.getGeneratedKeys(); // gets auto-generated keys like auto-increment
            int moneyID = 0;
            if (rs.next()) {
                moneyID = rs.getInt(1);
            }

            // updating the card table
            stCard = conn.prepareStatement("UPDATE card SET existingmoney_id = ? WHERE card_num = ?");
            stCard.setInt(1, moneyID);
            stCard.setString(2, cardNum);
            stCard.executeUpdate();

            conn.commit(); // save data
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
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
            Connection conn = getConnection();
            PreparedStatement st = null;
            ResultSet rs = null;

            if (conn == null) {
                message = "<font color='red'>Failed to connect to database.</font>"
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
                    System.out.printf(Account.Color.YELLOW + "BALANCE: " + Account.Color.RESET + "PHP %.2f%n", balance);
                } else {
                    message = "<font color='red'>Failed to retrieve balance. Please try again.</font>"
                            + "<br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
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

    public boolean isValidInput(String input) {
        return input != null && !input.isBlank();
    }

    // add category
    public void addCateg(String cardNum) {
        Connection conn = getConnection();
        String category;
        double budget;
        while (true) {
            System.out.print("Enter category name(eg. Food, Allowance, etc.): ");
            category = sc.nextLine();
            category = formatInput(category);
            if (!isValidInput(category)) {
                continue;
            }
            // check if categ alr exists
            try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM categories WHERE name = ? AND card_id = ?")) {
                ps.setString(1, category);
                ps.setInt(2, acc.getCardId());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    message = "<font color = red>Category " + category + " already exists." + "</font>";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                System.out.print("Enter estimated category budget for one day: ");
                budget = sc.nextDouble();
                sc.nextLine();
                break;
            } catch (InputMismatchException e) {
                sc.nextLine();
                message = "<font color = 'red'>Invalid input.</font>"
                        + " Please enter a number." + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
            }
        }
        int cardId = acc.getCardId();

        try (PreparedStatement st = conn
                .prepareStatement("INSERT INTO categories (name, budget, card_id) VALUES (?, ?, ?)")) {
            st.setString(1, category);
            st.setDouble(2, budget);
            st.setInt(3, cardId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        message = "Category " + "<font color='#9E00FF'>" + category + "</font>"
                + " has been added successfully!" + "<br>Click OK to continue.";
        title = "Success";
        Methods.showMessage(title, message);

    }

    // getting boolean value for first time user
    public boolean isFirstTime(String cardNum) {
        Connection conn = getConnection();

        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
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
                System.out.print("Do you want to add another category? (yes/no) ");
                String choice = sc.nextLine();

                if (!choice.equalsIgnoreCase("yes") || !choice.equalsIgnoreCase("y")) {
                    break;
                }
            }

        }
        return true;
    }

    // financial log
    public boolean FinancialLog() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
            int choice = 0;
            int dec = 0;
            do {

                while (true) {
                    try {
                        System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                                + "\n~~~~~~~~~~~~~~~~~~~~FINANCIAL LOG~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "1. View Essential Expenses" + Account.Color.RESET);
                        System.out
                                .println(Account.Color.PURPLE + "2. View Non-Essential Expenses" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "3. Filter Financial Log" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "4. Add Financial Log" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "5. Edit Financial Log" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "0. Return to Main Menu" + Account.Color.RESET);
                        System.out.print("Select from the following options: ");
                        choice = sc.nextInt();
                        sc.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        sc.nextLine();
                        message = "<font color = 'red'>Invalid input.</font>"
                                + " Please enter a number from the following."
                                + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                    }
                }
                switch (choice) {
                    case 1:
                        if (!expensesChecker(Account.status.ESSENTIALS.name())) {
                            dec = JOptionPane.showConfirmDialog(null,
                                    "<html><div style = 'font-family:Georgia; font-size: 12px;'><font color = red>NO financial record found!</font>"
                                            +
                                            "<br> Do you want to add a log?</div></html>",
                                    "WARNING!", JOptionPane.YES_NO_OPTION);
                            if (dec == JOptionPane.YES_OPTION) {
                                addFinLog();
                                continue;
                            } else {
                                break;
                            }
                        } else {
                            viewEssential();
                        }
                        continue;
                    case 2:
                        if (!expensesChecker(Account.status.TREATS.name())) {
                            dec = JOptionPane.showConfirmDialog(null,
                                    "<html><div style = 'font-family:Georgia; font-size: 12px;'><font color = red>NO financial record found!</font>"
                                            +
                                            "<br> Do you want to add a log?</div></html>",
                                    "WARNING!", JOptionPane.YES_NO_OPTION);
                            if (dec == JOptionPane.YES_OPTION) {
                                addFinLog();
                                continue;
                            } else {
                                break;
                            }
                        } else {
                            viewTreats();
                        }
                        break;
                    case 3:
                        if (!expensesChecker(Account.status.ESSENTIALS.name())
                                && !expensesChecker(Account.status.TREATS.name())) {
                            dec = JOptionPane.showConfirmDialog(null,
                                    "<html><div style = 'font-family:Georgia; font-size: 12px;'><font color = red>NO financial record found!</font>"
                                            +
                                            "<br> Do you want to add a log?</div></html>",
                                    "WARNING!", JOptionPane.YES_NO_OPTION);
                            if (dec == JOptionPane.YES_OPTION) {
                                addFinLog();
                                continue;
                            } else {
                                break;
                            }
                        } else {
                            int filterChoice = 0;
                            do {
                                while (true) {
                                    try {
                                        System.out
                                                .println(Account.Color.GREEN
                                                        + "\n==========FILTER FINANCIAL LOG=========="
                                                        + Account.Color.RESET);
                                        System.out
                                                .println(Account.Color.BLUE + "1. Filter by day" + Account.Color.RESET);
                                        System.out.println(
                                                Account.Color.BLUE + "2. Filter by week" + Account.Color.RESET);
                                        System.out.println(
                                                Account.Color.BLUE + "3. Filter by month" + Account.Color.RESET);
                                        System.out.println(Account.Color.BLUE + "0. Back" + Account.Color.RESET);
                                        System.out.print("Select from the following options: ");
                                        filterChoice = sc.nextInt();
                                        break;
                                    } catch (InputMismatchException e) {
                                        sc.nextLine();
                                        message = "<font color = 'red'>Invalid input.</font>"
                                                + " Please enter a number from the following."
                                                + "<br>Click OK to continue.";
                                        title = "WARNING!";
                                        Methods.showErrorMessage(title, message);
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
                                    case 0:
                                        break;
                                    default:
                                        message = "<font color='red'>Invalid number. Please try again</font>"
                                                + "<br>Click OK to continue.";
                                        title = "WARNING!";
                                        Methods.showErrorMessage(title, message);
                                }
                            } while (filterChoice != 0);
                        }

                        break;
                    case 4:
                        addFinLog();
                        break;
                    case 5:
                        editFinLog(acc.getCardNum());
                        break;
                    case 0:
                        return true;
                    default:
                        message = "<font color='red'>Invalid number. Please try again</font>"
                                + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                        break;
                }
            } while (choice != 0);
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
        String again = "yes";

        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return;
        }

        do {
            int choice = 0;
            while (true) {
                try {
                    System.out.println(
                            Account.Color.GREEN + "\n==========ADD FINANCIAL LOG==========" + Account.Color.RESET);
                    System.out.println(Account.Color.BLUE + "1. Add in a category (essentials)" + Account.Color.RESET);
                    System.out.println(Account.Color.BLUE + "2. No category (non-essentials)" + Account.Color.RESET);
                    System.out.println(Account.Color.BLUE + "0. Back" + Account.Color.RESET);
                    System.out.print("Select from the following options: ");
                    choice = sc.nextInt();
                    sc.nextLine(); // clear buffer
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    message = "<font color = 'red'>Invalid input.</font>"
                            + " Please enter a number from the following." + "<br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    continue;
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
                            message = "<html><div style = 'font-family:Georgia; font-size: 12px;'><font color = 'red'>No categories found!</font>"
                                    + "<br>Do you want to add a category?";
                            title = "WARNING!";
                            int categChoice = JOptionPane.showConfirmDialog(null, message, title,
                                    JOptionPane.YES_NO_OPTION);

                            if (categChoice == JOptionPane.YES_OPTION) {
                                addCategFlow();
                                break;
                            } else {
                                continue;
                            }
                        }
                        System.out.println("CATEGORIES: ");
                        for (int i = 0; i < categories.size(); i++) {
                            System.out.println((i + 1) + ". " + categories.get(i));
                        }
                        int categoryChoice;
                        while (true) {
                            try {
                                System.out.print("Select a category: ");
                                categoryChoice = sc.nextInt();
                                sc.nextLine(); // clear buffer
                                break;
                            } catch (InputMismatchException e) {
                                message = "<font color = red>Please enter a number.</font><br>Click OK to continue.";
                                title = "ERROR!";
                                Methods.showErrorMessage(title, message);
                            }
                        }

                        if (categoryChoice < 1 || categoryChoice > categories.size()) {
                            message = "<font color='red'>Invalid category choice! Please try again.</font>"
                                    + "<br>Click OK to continue.";
                            title = "WARNING!";
                            Methods.showErrorMessage(title, message);
                            break;
                        }
                        String selectedCategory = categories.get(categoryChoice - 1);
                        String expenseName;
                        while (true) {
                            try {
                                System.out.print("Enter the name of the expense: ");
                                expenseName = sc.nextLine();
                                expenseName = formatInput(expenseName);
                                if (!expenseName.isBlank()) {
                                    break;
                                }
                            } catch (InputMismatchException e) {
                                e.printStackTrace();
                            }
                        }
                        double expenseAmount;
                        while (true) {
                            try {
                                System.out.print("Enter the amount of the expense: ");
                                expenseAmount = sc.nextDouble();
                                sc.nextLine(); // clear buffer
                                break;
                            } catch (InputMismatchException e) {
                                sc.nextLine();
                                message = "<font color = 'red'>Invalid input.</font>"
                                        + " Please enter a number." + "<br>Click OK to continue.";
                                title = "WARNING!";
                                Methods.showErrorMessage(title, message);
                            }
                        }

                        try (PreparedStatement st = conn.prepareStatement(
                                "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE c.id = ?")) {
                            st.setInt(1, acc.getCardId());
                            ResultSet balRs = st.executeQuery();
                            double balance = 0;
                            if (balRs.next()) {
                                balance = balRs.getDouble("balance");

                            }
                            if (expenseAmount > balance) {
                                message = "<font color = red> Insufficient balance! Cannot proceed to add. </font><br> Please top-up your card again.";
                                title = "Transaction Rejected";
                                Methods.showErrorMessage(title, message);
                                return;
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        LocalDate expenseDate;
                        while (true) {
                            try {
                                System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                                expenseDate = LocalDate.parse(sc.nextLine());

                                LocalDate creationDate = acc.getExpiryDate().minusYears(1).toLocalDate();
                                LocalDate today = LocalDate.now();

                                boolean invalid = expenseDate.isBefore(creationDate)
                                        || expenseDate.isAfter(today);

                                if (invalid) {
                                    Methods.showErrorMessage("ERROR",
                                            "<font color='red'>Invalid date!</font> Please input valid date.");
                                    continue;
                                }
                                break;
                            } catch (DateTimeParseException e) {
                                message = "<font color = red>Invalid format! </font> Please use YYYY-MM-DD (e.g., 2026-01-31).";
                                title = "ERROR";
                                Methods.showErrorMessage(title, message);
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
                                message = "<font color='red'>Category not found! Please try again.</font>"
                                        + "<br>Click OK to continue.";
                                title = "WARNING!";
                                Methods.showErrorMessage(title, message);
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
                                message = "<font color='green'>Log added successfully!</font>"
                                        + "<br>Click OK to continue.";
                                title = "Success";
                                Methods.showMessage(title, message);

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
                            message = "<font color = red> Insufficient balance! Cannot proceed to add. </font><br> Please top-up your card again.";
                            title = "Transaction Rejected";
                            Methods.showErrorMessage(title, message);
                            continue;
                        }
                        LocalDate expenseDate;
                        while (true) {
                            try {
                                System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                                expenseDate = LocalDate.parse(sc.nextLine());

                                if (expenseDate.isBefore(acc.getExpiryDate().minusYears(1).toLocalDate())
                                        || expenseDate.isAfter(LocalDate.now())) {
                                    message = "<font color = red>Invalid date! </font> Please input valid date.";
                                    title = "ERROR";
                                    Methods.showErrorMessage(title, message);
                                    continue;
                                }
                                break;
                            } catch (DateTimeParseException e) {
                                message = "<font color = red>Invalid format! </font> Please use YYYY-MM-DD (e.g., 2026-01-31).";
                                title = "ERROR";
                                Methods.showErrorMessage(title, message);
                            }

                        }
                        try (PreparedStatement logPs = conn.prepareStatement(
                                "INSERT INTO expenses (price, task, date, status, card_id) VALUES (?, ?, ?, 'treats', ?)")) {
                            logPs.setDouble(1, expenseAmount);
                            logPs.setString(2, expenseName);
                            logPs.setDate(3, java.sql.Date.valueOf(expenseDate));
                            logPs.setInt(4, acc.getCardId());
                            logPs.executeUpdate();
                            message = "<font color='green'>Log added successfully!</font>"
                                    + "<br>Click OK to continue.";
                            title = "Success";
                            Methods.showMessage(title, message);

                        }
                        try (PreparedStatement balPs = conn.prepareStatement(
                                "UPDATE existingmoney em JOIN card c ON em.id = c.existingmoney_id SET em.balance = em.balance - ? WHERE c.id = ?")) {
                            balPs.setDouble(1, expenseAmount);
                            balPs.setInt(2, acc.getCardId());
                            balPs.executeUpdate();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case 0:
                    break;
                default:
                    message = "<font color='red'>Invalid number. Please try again</font>"
                            + "<br>Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                    break;
            }

            System.out.print("\nDo you want to add another log? (yes/no) ");
            again = sc.nextLine();
        } while (again.equalsIgnoreCase("yes") || again.equalsIgnoreCase("y"));

    }

    // view essential expenses
    public void viewEssential() {
        Connection conn = getConnection();
        if (conn == null) {
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
                essentials.add(String.format("%-10s | PHP %-5.2f | %-10s", task, price, date.toString()));
            }

            if (essentials.isEmpty()) {
                message = "<font color='red'>No essential expenses found!</font>"
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
                return;
            }

            System.out.println(Account.Color.GREEN + "\n==========ESSENTIAL EXPENSES==========="
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
                message = "<font color='red'>No non-essential expenses found!</font>"
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
                return;
            }

            System.out
                    .println(Account.Color.GREEN + "\n==========NON-ESSENTIAL EXPENSES==========="
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            message = "<font color='red'>Failed to connect to database.</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
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
            if (balance < 100) {
                message = "<font color='red'>Your balance is below PHP 100! Please top up your account.</font>"
                        + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
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
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
            int choice = 0;
            do {
                while (true) {
                    try {
                        System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                                + "\n~~~~~~~~~~~~~~~~~~~~VIEW BUDGET~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "1. Essential" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "2. Non-Essential" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "0. Back" + Account.Color.RESET);
                        System.out.print("Select from the following options:");
                        choice = sc.nextInt();
                        sc.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        sc.nextLine();
                        message = "<font color = 'red'>Invalid input.</font>"
                                + " Please enter a number from the following." + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                    }
                }

                switch (choice) {
                    case 1:
                        essentialBudget();
                        break;
                    case 2:
                        treatsBudget();
                        break;
                    case 0:
                        return true;
                    default:
                        message = "<font color='red'>Invalid number. Please try again</font>"
                                + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                        break;
                }
            } while (choice != 0);

        }
        return true;
    }

    public void essentialBudget() {
        if (!expensesChecker(Account.status.ESSENTIALS.name())) {
            message = "<font color='red'><br>NO financial record found!</font>"
                    + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
        } else {
            List<String> budgetAlloc = new ArrayList<>();
            Connection conn = getConnection();
            try (PreparedStatement bal = conn.prepareStatement(
                    "SELECT deposit FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                bal.setString(1, acc.getCardNum());
                ResultSet balRs = bal.executeQuery();
                double balance = 0;
                if (balRs.next()) {
                    balance = balRs.getDouble("deposit");
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
                                message = "<font color = red> Your expenses in " + name
                                        + " exceeds your daily budget! </font>";
                                title = "Budget Exceeded";
                                Methods.showErrorMessage(title, message);
                            }
                        }
                        if (balance > 0) {
                            percentBal = (expenses / balance) * 100;
                            if (percentBal > 100) {
                                message = "<font color = red> Your expenses in " + name
                                        + " exceeds your balance! </font>";
                                title = "Budget Exceeded";
                                Methods.showErrorMessage(title, message);
                            }
                        }

                        budgetAlloc.add(String.format(
                                "%-15s | PHP %-11.2f | PHP %-11.2f | %-16s | %-15s",
                                name, budget, expenses, String.format("%.2f%%", percentBudget),
                                String.format("%.2f%%", percentBal)));
                    }
                    System.out.println(Account.Color.GREEN + "\n==========ESSENTIAL BUDGET=========="
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
            message = "<font color='red'><br>NO financial record found!</font>"
                    + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
        } else {
            List<String> budgetAlloc = new ArrayList<>();
            Connection conn = getConnection();
            try (PreparedStatement bal = conn.prepareStatement(
                    "SELECT deposit FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                bal.setString(1, acc.getCardNum());
                ResultSet balRs = bal.executeQuery();
                double balance = 0;
                if (balRs.next()) {
                    balance = balRs.getDouble("deposit");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT IFNULL(SUM(e.price),0) AS total_expenses FROM expenses e  WHERE e.card_id = ? AND e.status = 'treats'")) {
                    ps.setInt(1, acc.getCardId());
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        double expenses = rs.getDouble("total_expenses");
                        double percentBal = 0;

                        if (balance > 0) {
                            percentBal = (expenses / balance) * 100;
                            if (percentBal > 100) {
                                message = "<font color = red> Your expenses exceeds your balance! </font><br>Click OK to continue.";
                                title = "Budget Exceeded";
                                Methods.showErrorMessage(title, message);
                            }
                        }
                        budgetAlloc.add(String.format("%-15s | PHP %-11.2f | %-15s", "All expenses",
                                expenses, String.format("%.2f%%", percentBal)));
                    }
                    System.out.println(
                            Account.Color.GREEN + "\n==========NON-ESSENTIAL BUDGET=========="
                                    + Account.Color.RESET);
                    System.out.println(
                            "%-15s | %-15s | %-15s".formatted("Category",
                                    "Total", "% used in Balance"));
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
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
            int choice = 0;
            do {
                while (true) {
                    try {
                        System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                                + "\n~~~~~~~~~~~~~~~~~~~~EDIT ACCOUNT DETAILS~~~~~~~~~~~~~~~~~~~~"
                                + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "1. Edit Name" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "2. Edit PIN" + Account.Color.RESET);
                        System.out.println(Account.Color.PURPLE + "0. Back" + Account.Color.RESET);
                        System.out.print("Pick from the following: ");
                        choice = sc.nextInt();
                        sc.nextLine();// clears buffer
                        break;
                    } catch (InputMismatchException e) {
                        sc.nextLine();
                        message = "<font color = 'red'>Invalid input.</font>"
                                + " Please enter a number from the following." + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                    }
                }

                switch (choice) {
                    case 1:
                        String newFirstName;
                        String newLastName;
                        while (true) {
                            System.out.print("Enter your new first name: ");
                            newFirstName = sc.nextLine();
                            newFirstName = formatInput(newFirstName);
                            if (!newFirstName.isBlank()) {
                                break;
                            }

                        }
                        while (true) {
                            System.out.print("Enter your new last name: ");
                            newLastName = sc.nextLine();
                            newLastName = formatInput(newLastName);
                            if (!newLastName.isBlank()) {
                                break;
                            }
                        }

                        try (Connection conn = getConnection();
                                PreparedStatement ps = conn.prepareStatement(
                                        "UPDATE cardholder SET first_name = ?, last_name = ? WHERE card_num = ?")) {
                            ps.setString(1, newFirstName);
                            ps.setString(2, newLastName);
                            ps.setString(3, acc.getCardNum());
                            int updated = ps.executeUpdate();
                            if (updated > 0) {
                                message = "<font color='green'>Name updated successfully!</font>"
                                        + "<br>Click OK to continue.";
                                title = "Success";
                                Methods.showMessage(title, message);

                                acc.setFirstName(newFirstName);
                                acc.setLastName(newLastName);
                            } else {
                                message = "<font color='red'>Failed to update name. Please try again.</font>"
                                        + "<br>Click OK to continue.";
                                title = "Error";
                                Methods.showErrorMessage(title, message);
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
                                acc.setHash(Authentication.hashPin(cardPin));
                                String hash = acc.getHash();
                                message = "Valid pin. Please remember your pin." + "<br>Click OK to continue.";
                                title = "SUCCESS!";
                                Methods.showMessage(title, message);

                                try (Connection conn = getConnection();
                                        PreparedStatement ps = conn.prepareStatement(
                                                "UPDATE cardholder SET hash = ? WHERE card_num = ?")) {
                                    ps.setString(1, hash);
                                    ps.setString(2, acc.getCardNum());
                                    int updated = ps.executeUpdate();
                                    if (updated > 0) {
                                        message = "<font color='green'>PIN updated successfully!</font>"
                                                + "<br>Click OK to continue.";
                                        title = "Success";
                                        Methods.showMessage(title, message);

                                    } else {
                                        message = "<font color='red'>Failed to update PIN. Please try again.</font>"
                                                + "<br>Click OK to continue.";
                                        title = "Error";
                                        Methods.showErrorMessage(title, message);
                                        break;
                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                message = "<font color='red'>Invalid pin. Please enter a 4 digit number.</font>"
                                        + "<br>Click OK to continue.";
                                title = "WARNING!";
                                Methods.showErrorMessage(title, message);
                                continue;
                            }
                            break;
                        }
                        break;
                    case 0:
                        return true;
                    default:
                        message = "<font color='red'>Invalid number. Please try again</font>"
                                + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                        break;

                }
            } while (choice != 0);
        }
        return true;
    }

    // deposit cash
    public boolean depositCash() {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return false;
        } else {
            System.out.println(Account.Color.VIOLET + Account.Color.BOLD
                    + "\n~~~~~~~~~~~~~~~~~~~~DEPOSIT CASH~~~~~~~~~~~~~~~~~~~~" + Account.Color.RESET);
            double amount;
            while (true) {
                try {
                    System.out.print("Enter the amount you want to deposit: ");
                    amount = sc.nextDouble();
                    sc.nextLine(); // clear buffer
                    System.out.print("Are you sure you want to deposit this amount? (yes/no): ");
                    String dec = sc.nextLine();

                    if (dec.equalsIgnoreCase("yes") || dec.equalsIgnoreCase("y")) {
                        break;
                    }
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    message = "<font color = red> Invalid input! </font>" +
                            " Please enter a number. <br> Click OK to continue.";
                    title = "WARNING!";
                    Methods.showErrorMessage(title, message);
                }

            }

            try (Connection conn = getConnection();
                    PreparedStatement ps = conn.prepareStatement(
                            "UPDATE existingmoney em JOIN card c ON em.id = c.existingmoney_id SET em.balance = em.balance + ?, em.deposit = em.deposit + ? WHERE c.card_num = ?")) {
                ps.setDouble(1, amount);
                ps.setDouble(2, amount);
                ps.setString(3, acc.getCardNum());
                ps.executeUpdate();
                message = String.format("<font color='green'>Amount PHP %.2f deposited successfully!</font>"
                        + "<br>Click OK to continue.", amount);
                title = "Success";
                Methods.showMessage(title, message);

                try (PreparedStatement bal = conn.prepareStatement(
                        "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE card_num = ?")) {
                    bal.setString(1, acc.getCardNum());
                    ResultSet balRs = bal.executeQuery();
                    double balance = 0;
                    if (balRs.next()) {
                        balance = balRs.getDouble("balance");
                    }

                    if (balance < 100) {
                        message = "<font color='red'>Your balance is still below PHP 100! Please top up your account.</font>"
                                + "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
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

    // edit financial log
    public void editFinLog(String cardNum) {
        if (auth.sessionChecker(acc.getCardNum()) == false) {
            message = "" + "Your session has expired. Please login again."
                    + "<font color='red'><br>LOGGING OUT...</font>" + "<br>Click OK to continue.";
            title = "WARNING!";
            Methods.showErrorMessage(title, message);
            return;
        } else {
            int choice = 0;
            do {
                while (true) {
                    try {
                        System.out.println(Account.Color.GREEN + Account.Color.BOLD
                                + "==========EDIT FINANCIAL LOG==========" + Account.Color.RESET);
                        System.out.println("1. Essentials");
                        System.out.println("2. Non-essentials");
                        System.out.println("0. Back");
                        System.out.print("Select category where you want to edit: ");
                        choice = sc.nextInt();
                        sc.nextLine();
                        break;
                    } catch (InputMismatchException e) {
                        sc.nextLine();
                        message = "<font color = red> Invalid input! </font> Input a number from the following." +
                                "<br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                    }
                }

                int logId;
                switch (choice) {
                    case 1:
                        try (Connection conn = getConnection();
                                PreparedStatement st = conn.prepareStatement(
                                        "SELECT id, task, price, date FROM expenses WHERE card_id = ? and status = 'essentials'")) {
                            st.setInt(1, acc.getCardId());
                            ResultSet rs = st.executeQuery();
                            double price = 0;

                            List<String> essentials = new ArrayList<>();
                            while (rs.next()) {
                                int id = rs.getInt("id");
                                String name = rs.getString("task");
                                price = rs.getDouble("price");
                                LocalDate date = rs.getDate("date").toLocalDate();
                                essentials.add(String.format("%-5s | %-10s | PHP %-5.2f | %-10s", id, name, price,
                                        date.toString()));
                            }

                            if (essentials.isEmpty()) {
                                message = "<font color = red> No financial record found. </font> <br> Click OK to continue.";
                                title = "ERROR!";
                                Methods.showErrorMessage(title, message);
                                return;
                            }

                            System.out.println(Account.Color.RED + "\nESSENTIAL LOGS" + Account.Color.RESET);
                            System.out
                                    .println("%-5s | %-11s | %-10s | %-10s".formatted("ID", "Name", "Amount", "Date"));
                            for (String expense : essentials) {
                                System.out.println(expense);
                            }
                            System.out.print("Enter ID you want to edit: ");
                            logId = sc.nextInt();
                            sc.nextLine(); // clear buffer
                            newFinLog(logId, price);

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                        break;
                    case 2:
                        try (Connection conn = getConnection();
                                PreparedStatement st = conn.prepareStatement(
                                        "SELECT id, task, price, date FROM expenses WHERE card_id = ? and status = 'treats'")) {
                            st.setInt(1, acc.getCardId());
                            ResultSet rs = st.executeQuery();
                            double price = 0;

                            List<String> treats = new ArrayList<>();
                            while (rs.next()) {
                                int id = rs.getInt("id");
                                String name = rs.getString("task");
                                price = rs.getDouble("price");
                                LocalDate date = rs.getDate("date").toLocalDate();
                                treats.add(String.format("%-5s | %-10s | PHP %-5.2f | %-10s", id, name, price,
                                        date.toString()));
                            }

                            if (treats.isEmpty()) {
                                message = "<font color = red> No financial record found. </font> <br> Click OK to continue.";
                                title = "ERROR!";
                                Methods.showErrorMessage(title, message);
                                return;
                            }

                            System.out.println(Account.Color.RED + "\nNON-ESSENTIAL  LOGS" + Account.Color.RESET);
                            System.out
                                    .println("%-5s | %-11s | %-10s | %-10s".formatted("ID", "Name", "Amount", "Date"));
                            for (String expense : treats) {
                                System.out.println(expense);
                            }
                            System.out.print("Enter ID you want to edit: ");
                            logId = sc.nextInt();
                            sc.nextLine(); // clear buffer
                            newFinLog(logId, price);

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0:
                        break;
                    default:
                        message = "<font color = red> Invalid input!</font>" +
                                " Please select a number from the following. <br>Click OK to continue.";
                        title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                }

            } while (choice != 0);
        }
    }

    // new financial log
    public void newFinLog(int id, double price) {
        Connection conn = getConnection();
        String newName;
        double newAmount;
        LocalDate newDate;
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM expenses where id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                message = "<font color = red>No category id found!</font>" +
                        "Please check your input and try again. <br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.print("Enter new name of the expense: ");
        newName = sc.nextLine();
        newName = formatInput(newName);
        while (true) {
            try {
                System.out.print("Enter the amount of the expense: ");
                newAmount = sc.nextDouble();
                sc.nextLine(); // clear buffer
                break;
            } catch (InputMismatchException e) {
                sc.nextLine();
                message = "<font color = 'red'>Invalid input.</font>"
                        + " Please enter a number." + "<br>Click OK to continue.";
                title = "WARNING!";
                Methods.showErrorMessage(title, message);
            }
        }

        while (true) {
            try {
                System.out.print("Enter the date of the expense (YYYY-MM-DD): ");
                newDate = LocalDate.parse(sc.nextLine());

                LocalDate creationDate = acc.getExpiryDate().minusYears(1).toLocalDate();
                LocalDate today = LocalDate.now();

                boolean invalid = newDate.isBefore(creationDate)
                        || newDate.isAfter(today);

                if (invalid) {
                    Methods.showErrorMessage("ERROR",
                            "<font color='red'>Invalid date!</font> Please input valid date.");
                    continue;
                }
                break;
            } catch (DateTimeParseException e) {
                message = "<font color = red>Invalid format! </font> Please use YYYY-MM-DD (e.g., 2026-01-31).";
                title = "ERROR";
                Methods.showErrorMessage(title, message);
            }

        }
        double deposit = 0;
        double balance = 0;
        try (PreparedStatement stDeposit = conn.prepareStatement(
                "SELECT deposit FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE c.id = ?")) {
            stDeposit.setInt(1, acc.getCardId());
            ResultSet rs = stDeposit.executeQuery();
            while (rs.next()) {
                deposit = rs.getDouble("deposit");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement st = conn.prepareStatement(
                "SELECT balance FROM existingmoney em JOIN card c ON em.id = c.existingmoney_id WHERE c.id = ?")) {
            st.setInt(1, acc.getCardId());
            ResultSet balRs = st.executeQuery();
            if (balRs.next()) {
                balance = balRs.getDouble("balance");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn
                    .prepareStatement("UPDATE expenses SET task = ?, price = ?, date = ? WHERE id = ?");
                    PreparedStatement ps1 = conn.prepareStatement(
                            "UPDATE existingmoney SET deposit = ?, balance = ? WHERE id = (SELECT existingmoney_id FROM card WHERE card_num = ?)")) {

                double newDeposit = deposit + price - newAmount;
                double newBalance = balance + price - newAmount;
                ps1.setDouble(1, newDeposit);
                ps1.setDouble(2, newBalance);
                ps1.setString(3, acc.getCardNum());
                ps1.executeUpdate();
                ps.setString(1, newName);
                ps.setDouble(2, newAmount);
                ps.setDate(3, java.sql.Date.valueOf(newDate));
                ps.setInt(4, id);
                ps.executeUpdate();

                if (newAmount > (balance + price)) {
                    System.out.println(newAmount + " llala" + balance + price);
                    conn.rollback();
                    message = "<font color = red> Insufficient balance! Cannot proceed to add. </font><br> Please top-up your card again.";
                    title = "Transaction Rejected";
                    Methods.showErrorMessage(title, message);
                    return;
                }
                conn.commit();
                message = "<font color='green'>Log updated successfully!</font>"
                        + "<br>Click OK to continue.";
                title = "Success";
                Methods.showMessage(title, message);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

}
