package Budgetbuddy;

import javax.swing.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Account acc = new Account();
        Authentication auth = new Authentication();
        dbConnection db = new dbConnection(acc, auth);
        Methods m = new Methods(acc, db, auth);

        Scanner sc = new Scanner(System.in);
        String dec = "yes";
        String message;
        String title;
        int choice;
        boolean inMainMenu;
        Object[] options1 = { "BYE", "CANCEL" };

        do {
            // starting menu
            while (true) { // while(true) ensures that the user inputs int not Strings
                try {
                    System.out.println(Account.Color.YELLOW + Account.Color.BOLD + "\n\nWelcome to your Budget Buddy!"
                            + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "1. Register" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "2. Login" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "3. System Details" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "4. Exit" + Account.Color.RESET);
                    System.out.print("Please pick from the following: ");
                    choice = sc.nextInt();
                    sc.nextLine(); // clear buffer
                    break;
                } catch (InputMismatchException e) {
                    message = "<html><font color = 'red'>Invalid input.</font>"
                            + " Please enter a number from the following." + "<br>Click OK to continue";
                    title = "Warning";
                    Methods.showErrorMessage(title, message);
                    sc.nextLine();
                }
            }
            switch (choice) {
                case 1:
                    db.register();
                    inMainMenu = m.MainMenu();
                    if (!inMainMenu) {
                        continue;
                    }
                    break;

                case 2:
                    boolean loginSuccess = db.login();
                    if (!loginSuccess) {
                        break;
                    }
                    inMainMenu = m.MainMenu();
                    if (!inMainMenu) {
                        continue;
                    }
                    break;
                case 3:
                    m.showSystemDetails();
                    continue;
                case 4:
                    JOptionPane.showOptionDialog(null,
                            "<html><div style = 'font-size:15px; font-family:Georgia; color: #2539f1;'>Thank you for using your Budget Buddy! Goodbye!</div></html>",
                            "SEE YOU! <3",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options1, options1[0]);
                    sc.close();
                    System.exit(0); // exits the system
                    break;
                default:
                    message = "<html><font color='red'>Invalid number.</font>" + " Please try again"
                            + "<br>Click OK to continue";
                    title = "Warning";
                    Methods.showErrorMessage(title, message);
                    break;
            }
            System.out.println("Login again? [yes/no]");
            dec = sc.nextLine();
            if (dec.equalsIgnoreCase("no") || dec.equalsIgnoreCase("n")) {
                JOptionPane.showOptionDialog(null,
                        "<html><div style = 'font-size:15px; font-family:Georgia; color: #2539f1;'>Thank you for using your Budget Buddy! Goodbye!</div></html>",
                        "SEE YOU! <3",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options1, options1[0]);
            }
        } while (dec.equalsIgnoreCase("yes") || dec.equalsIgnoreCase("y"));

    }

}
