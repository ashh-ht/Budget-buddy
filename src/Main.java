package src;

import javax.swing.*;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Methods m = new Methods();
        dbConnection db = new dbConnection();
        JOptionPane pane = new JOptionPane();
        Scanner sc = new Scanner(System.in);
        String dec = "yes";

        // 1do{
            //starting menu
            System.out.println("Welcome to your Budget Buddy!");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.print("Please pick from the following: ");
            int choice = sc.nextInt();
            sc.nextLine(); //clear buffer

            switch (choice) {
                case 1:
                    db.register();
                    //main menu
                    break;

                case 2:
                    try {
                        m.login();
                    } catch (IOException e) {
                        System.out.println("Incorrect input. Please input a number.");
                        return;
                    }
                    break;
            }
        // } while (dec.equalsIgnoreCase(dec));

    }
    
}
