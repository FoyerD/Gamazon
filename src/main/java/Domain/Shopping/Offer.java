package Domain.Shopping;

import java.util.List;
import java.util.UUID;

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
    private List<Double> prices;
    private List<String> approvedBy; // includes member and employees who approved the offer

    @Embedded
    private PaymentDetails paymentDetails;

    protected Offer() {} // JPA needs a default constructor

    public Offer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails) {
        this.offerId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId = productId;
        this.prices = List.of(newPrice);
        this.paymentDetails = paymentDetails;
        this.counterOffer = false;
        this.approvedBy = List.of(memberId);
    }

    public boolean counterOffer(String userId, double newPrice) {
        this.approvedBy = List.of(); // Reset approved employees for a new counter offer
        this.prices.add(newPrice);
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
        return prices.get(prices.size() - 1);
    }
    public String getId() { return offerId; }
    public String getMemberId() { return memberId; }
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public PaymentDetails getPaymentDetails() { return this.paymentDetails; }
}
