package Domain.ExternalServices;

import java.util.Date;

import Application.utils.Response;

public interface IExternalPaymentService {
    Response<Void> updatePaymentServiceURL(String url);
    Response<Boolean> handshake();
    Response<Integer> processPayment(String userSSN, String cardNumber, Date expiryDate, String cvv, String holder, double amount);
    Response<Boolean> cancelPayment(int transactionId);
}