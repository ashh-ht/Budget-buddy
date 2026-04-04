package budgetbuddy;

import java.time.LocalDateTime;

public class Account {
    private String firstName;
    private String lastName;
    private String cardNum;
    private String cardPin;
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
}
