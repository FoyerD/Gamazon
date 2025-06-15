package Infrastructure;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import Application.utils.Error;
import Application.utils.Response;
import Domain.ExternalServices.IExternalSupplyService;

@Service
public class ExternalSupplyService implements IExternalSupplyService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String URL;

    @Override
    public Response<Void> updateSupplyServiceURL(String url) {
        this.URL = url;
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
            throw new RuntimeException("Failed to post to external supply service: " + e.getMessage(), e);
        }
    }

    @Override
    public Response<Integer> supplyOrder(String name, String address, String city, String country, String zip) {
        try {
            Map<String, String> data = Map.of(
                "action_type", "supply",
                "name", name,
                "address", address,
                "city", city,
                "country", country,
                "zip", zip
            );
            String result = post(data);
            int transactionId = Integer.parseInt(result);
            if (transactionId == -1) {
                return new Response<>(new Error("Supply failed"));
            }
            return new Response<>(transactionId);
        } catch (Exception e) {
            return new Response<>(new Error("Supply error: " + e.getMessage()));
        }
    }

    @Override
    public Response<Boolean> cancelSupply(int transactionId) {
        try {
            Map<String, String> data = Map.of(
                "action_type", "cancel_supply",
                "transaction_id", String.valueOf(transactionId)
            );
            String result = post(data);
            return new Response<>("1".equals(result));
        } catch (Exception e) {
            return new Response<>(new Error("Failed to cancel supply: " + e.getMessage()));
        }
    }
}
