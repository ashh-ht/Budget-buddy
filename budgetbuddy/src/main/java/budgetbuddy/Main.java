package Budgetbuddy;

import javax.swing.*;

import java.io.IOException;
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
        int choice;
        Object[] options = { "OK", "CANCEL" };
        Object[] options1 = { "BYE", "CANCEL" };
        do {
            // starting menu
            while (true) { // while(true) ensures that the user inputs int not Strings
                try {
                    System.out.println(Account.Color.YELLOW + Account.Color.BOLD + "\n\nWelcome to your Budget Buddy!"
                            + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "1. Register" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "2. Login" + Account.Color.RESET);
                    System.out.println(Account.Color.PURPLE + "3. Exit" + Account.Color.RESET);
                    System.out.print("Please pick from the following: ");
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
                    db.register();
                    m.MainMenu();
                    break;

                case 2:
                    try {
                        boolean loginSuccess = db.login();
                        if (!loginSuccess) {
                            continue;
                        }
                    } catch (IOException e) {
                        JOptionPane.showOptionDialog(null,
                                "<html><font color = 'red'>Invalid input.</font></html>"
                                        + " Please enter a number from the following." + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                        return;
                    }
                    m.MainMenu();
                    break;
                case 3:
                    JOptionPane.showOptionDialog(null, "Thank you for using your Budget Buddy! Goodbye!",
                            "SEE YOU! <3",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options1, options1[0]);
                    System.exit(0); // exits the system
                    break;
                default:
                    JOptionPane.showOptionDialog(null, "<html><font color='red'>Invalid number.</font></html>" + " Please try again" + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }
            System.out.println("Login again? [yes/no]");
            dec = sc.nextLine();
            if (dec.equalsIgnoreCase("no") || dec.equalsIgnoreCase("n")) {
                JOptionPane.showOptionDialog(null, "<html><font color='#0017e6'>Thank you for using your Budget Buddy! Goodbye!</font></html>",
                        "SEE YOU! <3",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, options1, options1[0]);
            }
        } while (dec.equalsIgnoreCase("yes") || dec.equalsIgnoreCase("y"));

    }

}
