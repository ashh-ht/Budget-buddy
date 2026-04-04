package budgetbuddy;

import java.util.*;
import javax.swing.JOptionPane;
import java.time.LocalDateTime;

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

    public static String generateCardNum() {
        Random random = new Random();
        int[] randomNum = { 19, 28, 37, 46, 55, 64, 73, 82, 91 }; //numbers that adds up to 10 as a card num checker
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

    //formatting the account details
    public void AccountDetails() {
        String firstName = acc.getFirstName();
        String lastName = acc.getLastName();
        String cardNum = acc.getCardNum();
        LocalDateTime expiryDate = acc.getExpiryDate();
        String name = firstName + " " + lastName;
        System.out.print("\n\nACCOUNT NAME: " + name);
        System.out.println("\nCARD NUMBER: " + cardNum);
        System.out.println("CARD EXPIRATION DATE: " + expiryDate.getMonth() + " " + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear());
    }
    
    //MAIN MENU SWITCH
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
            int choice = sc.nextInt();
            sc.nextLine();//clears buffer

            switch (choice) {
                case 1: 
                    //view balance func
                    break;
                case 2:
                    //view budget func
                    break;
                case 3:
                    //view deposit cash
                    break;
                case 4:
                    //view finan log
                    break;
                case 5:
                    //insert bud categ
                    break;
                case 6:
                    //update database details
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
}
