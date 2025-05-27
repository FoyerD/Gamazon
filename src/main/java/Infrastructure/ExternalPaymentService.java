package Infrastructure;

import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.ExternalServices.IExternalPaymentService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import Application.utils.Error;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class ExternalPaymentService implements IExternalPaymentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String URL = "https://damp-lynna-wsep-1984852e.koyeb.app/";

    @Override
    public Response<Void> updatePaymentServiceURL(String newUrl) {
        this.URL = newUrl;
        return null;
    }

    @Override
    public Response<Boolean> handshake() {
        try {
            Map<String, String> data = Map.of("action_type", "handshake");
            String result = post(data);
            return new Response<>("OK".equalsIgnoreCase(result));
        } catch (Exception e) {
            return new Response<>(new Error("Handshake failed: " + e.getMessage()));
        }
    }

    private String post(Map<String, String> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.setAll(body);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            return restTemplate.postForObject(URL, request, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to post to external payment service: " + e.getMessage(), e);
        }
    }

    @Override
    public Response<Integer> processPayment(String userId, String cardNumber, Date expiryDate, String cvv, String holder, double amount) {
        try {            
            Map<String, String> data = Map.of(
                "action_type", "pay" ,
                "amount", String.valueOf((int) amount),
                "currency", "USD" ,
                "card_number", cardNumber,
                "month", String.valueOf(expiryDate.getMonth() + 1),
                "year", String.valueOf(expiryDate.getYear() + 1900),
                "holder", holder,
                "cvv", cvv ,
                "id", userId
                );
            String result = post(data);
            int transactionId = Integer.parseInt(result);
            if (transactionId == -1) {
                return new Response<>(new Error("Payment failed"));
            }
            return new Response<>(transactionId);
        } catch (Exception e) {
            return new Response<>(new Error("Payment error: " + e.getMessage()));
        }
    }

    @Override
    public Response<Boolean> cancelPayment(int transactionId) {
        try {
            Map<String, String> data = Map.of(
                "action_type", "cancel_pay",
                "transaction_id", String.valueOf(transactionId)
            );
            String result = post(data);
            return new Response<>("1".equals(result));
        } catch (Exception e) {
            return new Response<>(new Error("Failed to cancel payment: " + e.getMessage()));
        }
    }
}
