package src;

import javax.swing.*;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Account acc = new Account();
        Methods m = new Methods(acc);
        dbConnection db = new dbConnection(acc,m);
        Scanner sc = new Scanner(System.in);
        String dec = "yes";
        int choice;
        Object[] options = { "OK", "CANCEL" };
        do{
        //starting menu
            while (true) { //while(true) ensures that the user inputs int not Strings
                try {
                    System.out.println("\n\nWelcome to your Budget Buddy!");
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println("3. Exit");
                    System.out.print("Please pick from the following: ");
                    choice = sc.nextInt();
                    sc.nextLine(); //clear buffer
                    break;
                } catch (InputMismatchException e) {
                    sc.nextLine();
                    JOptionPane.showOptionDialog(null, "Invalid inpuut. Please enter a number from the following." + "\nClick OK to continue",
                            "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    
                }
            }
                switch (choice) {
                    case 1:
                        db.register();
                        m.MainMenu();
                        //main menu
                        break;

                    case 2:
                        try {
                            boolean loginSuccess = db.login();
                            if (!loginSuccess) {
                                continue;
                            }
                        } catch (IOException e) {
                            JOptionPane.showOptionDialog(null,
                                    "Invalid inpuut. Please input a number." + "\nClick OK to continue",
                                    "Warning",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                    null, options, options[0]);
                            return;
                        }
                        m.MainMenu();
                        break;
                    case 3:
                        Object[] options1 = { "BYE", "CANCEL" };
                        JOptionPane.showOptionDialog(null, "Thank you for using your Budget Buddy! Goodbye!",
                                "SEE YOU! <3",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                null, options1, options1[0]);
                        System.exit(0); //exits the system
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
