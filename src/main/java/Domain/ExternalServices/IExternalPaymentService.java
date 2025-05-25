package Domain.ExternalServices;

import Application.utils.Response;

import java.util.Date;

public interface IExternalPaymentService {
    Response<Void> updatePaymentServiceURL(String url);
    Response<Boolean> handshake();
    Response<Integer> processPayment(String userId, String cardNumber, Date expiryDate, String cvv, String holder, double amount);
    Response<Boolean> cancelPayment(int transactionId);
}