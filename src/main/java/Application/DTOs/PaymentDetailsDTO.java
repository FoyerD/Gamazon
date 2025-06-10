package Application.DTOs;

import java.time.LocalDate;

import Domain.Shopping.PaymentDetails;

public class PaymentDetailsDTO {
    private final String userId;
    private final String cardNumber;
    private final LocalDate expiryDate;
    private final String cvv;
    private final String holder;

    public PaymentDetailsDTO(String userId, String cardNumber, LocalDate expiryDate, String cvv, String holder) {
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.holder = holder;
    }

    public PaymentDetailsDTO(PaymentDetails paymentDetails) {
        this.userId = paymentDetails.getUserId();
        this.cardNumber = paymentDetails.getCardNumber();
        this.expiryDate = paymentDetails.getExpiryDate();
        this.cvv = paymentDetails.getCvv();
        this.holder = paymentDetails.getHolder();
    }

    public static PaymentDetailsDTO from(PaymentDetails paymentDetails) {
        return new PaymentDetailsDTO(paymentDetails);
    }

    public PaymentDetails toPaymentDetails() {
        return new PaymentDetails(this.userId, this.cardNumber, this.expiryDate, this.cvv, this.holder);
    }

    public String getUserId() { return userId; }
    public String getCardNumber() { return cardNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public String getCvv() { return cvv; }
    public String getHolder() { return holder; }
}
