package Infrastructure;

import java.util.Date;

import Application.Response;
import Domain.ExternalServices.IPaymentService;

public class PaymentService implements IPaymentService {
    
    private String paymentServiceURL;
    
    public PaymentService(String paymentServiceURL) {
        this.paymentServiceURL = paymentServiceURL;
    }

    @Override
    public void updatePaymentServiceURL(String url) {
        this.paymentServiceURL = url;
    }

    public String getPaymentServiceURL() {
        return paymentServiceURL;
    }

    @Override
    public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv, double price,
            long andIncrement, String name, String deliveryAddress) {
        // Implementation for processing payment with additional parameters
        System.out.println("Processing payment for " + name + " at " + deliveryAddress);
        return new Response<Boolean>();
    }
} 
