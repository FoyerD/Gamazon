package Domain.ExternalServices;

import java.util.Date;

public interface IPaymentService {
    void updatePaymentServiceURL(String url);

    boolean processPayment(String card_owner, String card_number, Date expiry_date, String cvv, double price,
            long andIncrement, String name, String deliveryAddress);
}
