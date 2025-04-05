package Infrastructure;

import java.util.Date;

import Application.Responses.Response;

public interface IPaymentService {

    void updatePaymentServiceURL(String url);

    Response processPayment(String card_owner, String card_number, Date expiry_date, String cvv, double price,
            long andIncrement, String name, String deliveryAddress);
   
} 
