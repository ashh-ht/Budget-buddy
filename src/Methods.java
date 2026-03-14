package src;

import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;

public class Methods {
    public static void generateCardNum() {
        Random random = new Random();
        int[] randomNum = { 19, 28, 37, 46, 55, 64, 73, 82, 91 }; //numbers that adds up to 10 as a card num checker
        int numOfDigits = 14;
        int maxRange = 10;
        int randomIndex = random.nextInt(randomNum.length);

        System.out.print("Your card number is: ");
        for (int i = 0; i < numOfDigits; i++) {
            int digit = random.nextInt(maxRange);
            System.out.print(digit);
        }
        System.out.print(randomNum[randomIndex]);
    }

    public void expiryDate() {
        LocalDateTime date = LocalDateTime.now(); //get the time and date when the user inputted smth
        LocalDateTime expiryDate = date.plusYears(1); //add 1 yr for expiry date
        System.out.println("\n\nPlease save your card number for future use.");
        System.out.println("Your expiry date is: " + expiryDate.getMonth() + " " + expiryDate.getDayOfMonth() + ", "
                + expiryDate.getYear());
    }

    //check if the card is valid or not
    public void cardNumChecker(int cardNum) {
        long lastDigit = cardNum % 100000000000000L; //get the last number of the card num
        long secondLastDigit = (cardNum / 10) % 10; //get the second last number
        long sum = lastDigit + secondLastDigit; //add the last and second last number

        if (sum == 10) {
            System.out.println("Valid card number!"); 
        } else {
            System.out.println("Invalid card number. Please try again.");
            return;
        }
    }

    public void login() throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter you card number: ");
        int cardNum = sc.nextInt();
        System.out.print("Enter your card pin: ");
        int cardPin = sc.nextInt();

        cardNumChecker(cardNum);
        dbConnection.checkUser(cardNum, cardPin);
        dbConnection.checkExpiry(cardNum);

        sc.close();
    }
}
