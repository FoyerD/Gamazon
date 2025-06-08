package Domain.Shopping;

import java.time.LocalDate;

public class PaymentDetails {

    private final String userId;
    private final String cardNumber;
    private final LocalDate expiryDate;
    private final String cvv;
    private final String holder;

    public PaymentDetails(String userId, String cardNumber, LocalDate expiryDate, String cvv, String holder) {
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.holder = holder;
    }

    public String getUserId() { return userId; }
    public String getCardNumber() { return cardNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
    public String getHolder() { return holder; }
}
