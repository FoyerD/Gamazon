package Domain.Shopping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Domain.Pair;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Offer {

    // !TODO: add Persistence annotations for JPA
    @Id
    private String offerId;

    private String memberId;
    private String storeId;
    private String productId;
    
    private boolean counterOffer;
    private List<Pair<String, Double>> prices;
    private List<String> approvedBy; // includes member and employees who approved the offer

    @Embedded
    private PaymentDetails paymentDetails;

    protected Offer() {} // JPA needs a default constructor

    public Offer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails) {
        this.offerId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId = productId;
        this.prices = new ArrayList<>(List.of(new Pair<>(memberId, newPrice)));
        this.paymentDetails = paymentDetails;
        this.counterOffer = false;
        this.approvedBy = new ArrayList<>(List.of(memberId));
    }

    public boolean counterOffer(String userId, double newPrice) {
        this.approvedBy = new ArrayList<>(); // Reset approved employees for a new counter offer
        this.prices.add(new Pair<>(userId, newPrice));
        // Check if employee makes a counter offer or the member
        this.counterOffer = userId != memberId;
        this.approvedBy.add(userId);
        return counterOffer;
    }

    public boolean isCounterOffer() { return counterOffer; }
    public double getLastPrice() {
        if (prices.isEmpty()) {
            throw new IllegalStateException("No prices available for this offer.");
        }
        return prices.get(prices.size() - 1).getSecond();
    }
    public String getId() { return offerId; }
    public String getMemberId() { return memberId; }
    public List<String> getApprovedBy() { return approvedBy; }
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public PaymentDetails getPaymentDetails() { return this.paymentDetails; }
    public List<Pair<String, Double>> getPrices() { return prices;}
}
