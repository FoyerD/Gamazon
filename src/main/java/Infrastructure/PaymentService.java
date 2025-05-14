package Infrastructure;

import java.util.Date;

import org.springframework.stereotype.Service;

import Application.utils.Response;
import Domain.ExternalServices.IPaymentService;

@Service
public class PaymentService implements IPaymentService {
    
    private String paymentServiceURL = "www.google.com";
    

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

    public void initialize() {
        // Initialization logic for the payment service
        System.out.println("Payment service initialized with URL: " + paymentServiceURL);
    }
} 
