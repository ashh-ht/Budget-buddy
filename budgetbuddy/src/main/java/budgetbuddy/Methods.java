package Budgetbuddy;

import java.util.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

public class Methods {
    Object[] options = { "OK", "CANCEL" };
    Account acc;
    Authentication auth;
    dbConnection db;

    public Methods(Account acc, dbConnection db, Authentication auth) {
        this.acc = acc;
        this.db = db;
        this.auth = auth;
    }

    Scanner sc = new Scanner(System.in);

    public static String generateCardNum() {
        Random random = new Random();
        int[] randomNum = { 19, 28, 37, 46, 55, 64, 73, 82, 91 }; // numbers that adds up to 10 as a card num checker
        int numOfDigits = 14;
        int maxRange = 10;
        int randomIndex = random.nextInt(randomNum.length);
        String cardNum = "";
        System.out.print("Your card number is: ");
        for (int i = 0; i < numOfDigits; i++) {
            int digit = random.nextInt(maxRange);
            cardNum += digit;
            System.out.print(digit);
        }
        System.out.print(randomNum[randomIndex]);
        cardNum += randomNum[randomIndex];
        return cardNum;
    }

    public static String timeFormatter(LocalDateTime time) {
        DateTimeFormatter standardFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String standardTime = time.format(standardFormatter);
        return standardTime;
    }

    // MAIN MENU SWITCH
    public boolean MainMenu() {

        boolean inMainMenu = false;
        db.AccountDetails();

        while (!inMainMenu) {
            int choice;
            while (true) {
                try {
                    System.out.println(Account.Color.YELLOW + Account.Color.BOLD + "\n==========MAIN MENU=========="
                            + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "1. View Balance" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "2. View Budget" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "3. Deposit Cash" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "4. View Financial Log" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "5. Insert Budget Category" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "6. Edit Account Details" + Account.Color.RESET);
                    System.out.println(Account.Color.BLUE + "7. Done" + Account.Color.RESET);
                    System.out.print("\nPick from the following: ");
                    choice = sc.nextInt();
                    sc.nextLine();// clears buffer
                    break;
                } catch (InputMismatchException e) {
                    String title = "ERROR!";
                    String message = "<html><font color = 'red'>Invalid input.</font>"
                            + " Please enter a number from the following." + "<br>Click OK to continue";
                    showErrorMessage(title, message);
                    sc.nextLine();
                }

            }
            boolean loggedIn = false;
            switch (choice) {
                case 1:
                    while (true) {
                        loggedIn = db.viewBalance();
                        if (!loggedIn) {
                            return false;
                        }
                        break;
                    }
                    break;
                case 2:
                    db.balanceChecker();
                    if (!db.expensesChecker(Account.status.ESSENTIALS.name())
                            && !db.expensesChecker(Account.status.TREATS.name())) {
                        String message = "<font color='red'><br>NO financial record found!</font>"
                                + "<br>Click OK to continue.";
                        String title = "WARNING!";
                        Methods.showErrorMessage(title, message);
                    } else {
                        while (true) {
                            loggedIn = db.viewBudget();
                            if (!loggedIn) {
                                return false;
                            }
                            break;
                        }
                    }
                    break;
                case 3:
                    while (true) {
                        loggedIn = db.depositCash();
                        if (!loggedIn) {
                            return false;
                        }
                        break;
                    }
                    break;
                case 4:
                    while (true) {
                        loggedIn = db.FinancialLog();
                        if (!loggedIn) {
                            return false;
                        }
                        break;
                    }
                    break;
                case 5:
                    while (true) {
                        loggedIn = db.addCategFlow();
                        if (!loggedIn) {
                            return false;
                        }
                        break;
                    }
                    break;
                case 6:
                    while (true) {
                        loggedIn = db.editAccDetails();
                        if (!loggedIn) {
                            return false;
                        }
                        break;
                    }
                    break;
                case 7:
                    inMainMenu = true;
                    break;
                default:
                    String message = "<html><font color='red'>Invalid number. Please try again</font>"
                            + "<br>Click OK to continue";
                    String title = "Warning";
                    showErrorMessage(title, message);
                    break;
            }
        }
        return false;
    }

    // system details
    public void showSystemDetails() {
        String welcome = "<html><div style = 'font-size:15px;'><font color = #8a3500>" +
                "<b>WELCOME TO BUDGET BUDDY! </b></font><br><br><b>Budget Buddy</b> is your personal budget management buddy. It will help you manage your expenses!"
                + "</div></html>";
        String welcomeTitle = "WELCOME!";
        scrollMessage(welcomeTitle, welcome);

        String intro = "<html><div style = 'width:250px; font-size: 13px;'><font color = #8a3500><b>Budget Buddy </b></font> is a simple budgeting system made for students to help them <font color = red>manage their money </font> better. "
                +
                "It works like an ATM-style app where users can <font color = blue><b>check their balance, add money, and record their daily spending</b></font>. "
                +
                "It also shows how much of their balance has been spent so they can easily see <font color = red>where their money goes</font>.<br>"
                +
                "The system helps students track their expenses daily, weekly, or monthly, so they can avoid overspending and learn how to budget wisely.</div></html>";
        String introTitle = "What is it all about?";
        scrollMessage(introTitle, intro);

        String features = "<html><div style = 'font-size: 12px;'><font color=blue><b>Budget Buddy Features:</b></font><br>"
                +
                "- View Balance <br>" +
                "- View Expenses Percent on Balance <br>" +
                "- Deposit Money <br>" +
                "- Add, View, Filter, and Edit Financial Logs <br>" +
                "- Separate Essential to Non-Essential Expenses <br>" +
                "- Edit Card Details </div></html>";
        String featuresTitle = "FEATURES";
        scrollMessage(featuresTitle, features);

    }

    public static void scrollMessage(String title, String message) {
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");

        textPane.setText(message);

        textPane.setEditable(false);
        textPane.setBackground(UIManager.getColor("Panel.background"));
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(380, 220));
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JOptionPane optionPane = new JOptionPane(
                scrollPane,
                JOptionPane.INFORMATION_MESSAGE);

        JDialog dialog = optionPane.createDialog(title);

        dialog.setSize(420, 260);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void showErrorMessage(String title, String message) {
        String messageStyled = "<html><div style='font-size:12px; font-family:Verdana; font-weight:bold;'>" + message
                + "</div></html>";
        JOptionPane pane = new JOptionPane(messageStyled, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = pane.createDialog(title);

        dialog.setSize(300, 200);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static void showMessage(String title, String message) {
        String messageStyled = "<html><div style = 'font-size:12px; font-family:Georgia; font-weight: bold;'>" + message
                + "</div></html>";
        JOptionPane pane = new JOptionPane(messageStyled, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(title);

        dialog.setSize(300, 200);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

}
