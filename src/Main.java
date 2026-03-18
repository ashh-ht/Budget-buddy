package src;

import javax.swing.*;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // --- INITIALIZATION (Gatekeeper) ---
        Account acc = new Account();
        Methods m = new Methods(acc);
        dbConnection db = new dbConnection(acc, m);
        
        // --- SYLVIA'S BRIDGE ---
        // Importante ito para magamit ni Methods ang mga database functions sa dbConnection
        m.setDb(db); 

        Scanner sc = new Scanner(System.in);
        String dec = "yes";
        int choice;
        Object[] options = { "OK", "CANCEL" };

        do {
            // --- STARTING MENU LOOP ---
            while (true) { 
                try {
                    System.out.println("\n\nWelcome to your Budget Buddy!");
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Please pick from the following: ");
                    choice = sc.nextInt();
                    sc.nextLine(); // clear buffer
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null, "Invalid input. Please enter a number from the following." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                }
            }

            switch (choice) {
                case 1:
                    db.register();
                    m.MainMenu(); // Papasok sa Main Menu pagkatapos ng registration
                    break;

                case 2:
                    try {
                        boolean loginSuccess = db.login();
                        if (!loginSuccess) {
                            continue; // Balik sa Welcome Screen kung failed ang login
                        }
                    } catch (IOException e) {
                        JOptionPane.showOptionDialog(null,
                                "Invalid input. Please input a number." + "\nClick OK to continue",
                                "Warning",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options, options[0]);
                        return;
                    }
                    m.MainMenu(); // Papasok sa Main Menu pagkatapos ng successful login
                    break;

                case 3:
                    Object[] options1 = { "BYE", "CANCEL" };
                    JOptionPane.showOptionDialog(null, "Thank you for using your Budget Buddy! Goodbye!",
                            "SEE YOU! <3",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options1, options1[0]);
                    System.exit(0); 
                    break;

                default: 
                    JOptionPane.showOptionDialog(null, "Invalid number. Please try again" + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }

            System.out.println("Login again? [yes/no]");
            dec = sc.nextLine();

        } while (dec.equalsIgnoreCase("yes") || dec.equalsIgnoreCase("y"));
    }
}