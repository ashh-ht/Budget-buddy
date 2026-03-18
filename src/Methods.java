package src;

import java.util.*;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;

public class Methods {
    Object[] options = { "OK", "CANCEL" };
    Account acc;
    dbConnection db; 

    public Methods(Account acc) {
        this.acc = acc;
    }

    public void setDb(dbConnection db) {
        this.db = db;
    }

    public static String generateCardNum() {
        Random random = new Random();
        int[] randomNum = { 19, 28, 37, 46, 55, 64, 73, 82, 91 }; 
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

    public boolean cardNumChecker(String cardNum) {
        if (cardNum == null || cardNum.length() < 2) return false;
        long lastDigit = Long.parseLong(cardNum) % 10;
        long secondLastDigit = (Long.parseLong(cardNum) / 10) % 10;
        long sum = lastDigit + secondLastDigit;

        if (sum == 10) {
            JOptionPane.showMessageDialog(null, "Valid Card Number!");
            return true;
        } else {
            JOptionPane.showOptionDialog(null, "Invalid card number. Please check your input and try again" + "\nClick OK to continue", "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return false;
        }
    }

    public void AccountDetails() {
        String firstName = acc.getFirstName();
        String lastName = acc.getLastName();
        String cardNum = acc.getCardNum();
        LocalDateTime expiryDate = acc.getExpiryDate();
        String name = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
        System.out.print("\n\nACCOUNT NAME: " + name);
        System.out.println("\nCARD NUMBER: " + cardNum);
        if (expiryDate != null) {
            System.out.println("CARD EXPIRATION DATE: " + expiryDate.getMonth() + " " + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear());
        }
    }
    
    public void MainMenu() {
        Scanner sc = new Scanner(System.in);
        boolean inMainMenu = false;
        AccountDetails();

        while (!inMainMenu) {
            System.out.println("\n========MAIN MENU========");
            System.out.println("1. View Balance");
            System.out.println("2. View Budget");
            System.out.println("3. Deposit Cash");
            System.out.println("4. View Financial Log");
            System.out.println("5. Insert Budget Category");
            System.out.println("6. Edit Account Details");
            System.out.println("7. Done");
            System.out.print("\nPick from the following: ");
            
            int choice = -1;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine(); 
            } else {
                sc.nextLine();
                continue;
            }

            switch (choice) {
                case 1: 
                    db.viewBalanceDetails();
                    break;
                case 5:
                    handleInsertCategory(sc);
                    break;
                case 7:
                    inMainMenu = true;
                    break;
                default:
                    JOptionPane.showOptionDialog(null,
                            "Invalid number. Please try again" + "\nClick OK to continue", "Warning",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                            null, options, options[0]);
                    break;
            }
        }
    }

    private void handleInsertCategory(Scanner sc) {
        int categCount = 0; 
        String addAnother = "yes"; // Default to start the loop

        System.out.println("\n========Add Budget Categories========");

        do {
            // Step 1: Enter Category Name (Walang examples para malinis)
            System.out.print("\nEnter Category Name: ");
            String name = sc.nextLine();

            // Step 2: Input expenses with validation
            System.out.print("Input estimated expenses in one(1) day: ");
            while (!sc.hasNextDouble()) {
                System.out.println("Invalid input. Please enter a number.");
                sc.next();
            }
            double budget = sc.nextDouble();
            sc.nextLine(); // BUFFER FIX: Nililinis ang memory para hindi mag-spam ang loop

            // Step 3: Save to DB
            db.insertCategoryToDB(name, "Essentials", budget);
            categCount++;

            // Step 4: Minimum of 2 categories logic (Silent loop base sa screenshot mo)
            if (categCount < 2) {
                addAnother = "yes";
            } else {
                // Step 5: Ask if user wants to add more (Lalabas lang after maka-2 categories)
                System.out.println("\nADD ANOTHER CATEGORY?");
                System.out.println("1. Yes");
                System.out.println("2. No");
                System.out.print("Choice: ");
                String choice = sc.nextLine();
                
                if (choice.equals("1") || choice.equalsIgnoreCase("yes") || choice.equalsIgnoreCase("y")) {
                    addAnother = "yes";
                } else {
                    addAnother = "no";
                }
            }

        } while (addAnother.equalsIgnoreCase("yes"));

        // Step 6: Final terminal output base sa flowchart labels
        System.out.println("\nUpdate budget"); 
        System.out.println("Back to Menu"); 
    }
}