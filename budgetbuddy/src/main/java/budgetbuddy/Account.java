package Budgetbuddy;

import java.time.LocalDateTime;

public class Account {
    private String firstName;
    private String lastName;
    private String cardNum;
    private String cardPin;
    private int cardId;
    private LocalDateTime expiryDate;
    private String hash;

    // getters
    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getCardNum() {
        return this.cardNum;
    }

    public String getCardPin() {
        return this.cardPin;
    }

    public LocalDateTime getExpiryDate() {
        return this.expiryDate;
    }

    public String getHash() {
        return this.hash;
    }
    
    public int getCardId() {
        return this.cardId;
    }

    // setters
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }                           

    class Color {
        public static final String RESET = "\u001B[0m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String PURPLE = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String BOLD = "\u001B[1m";
        public static final String VIOLET = "\u001B[38;5;129m";
    }

    public enum status {
        ESSENTIALS,
        TREATS
    }
}
