package Domain.Shopping;

import java.time.LocalDate;

import jakarta.persistence.Embeddable;

@Embeddable
public class PaymentDetails {

    private String userId;
    private String cardNumber;
    private LocalDate expiryDate;
    private String cvv;
    private String holder;

    protected PaymentDetails() {} // Required by JPA

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
