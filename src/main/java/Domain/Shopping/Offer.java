package Domain.Shopping;

import java.util.*;

import Domain.Pair;
import jakarta.persistence.*;

@Entity
public class Offer {

    @Id
    private String offerId;

    private String memberId;
    private String storeId;
    private String productId;
    private boolean counterOffer;
    private boolean isAccepted;

    @Embedded
    private PaymentDetails paymentDetails;

    @Embedded
    private SupplyDetails supplyDetails;

    @ElementCollection
    @CollectionTable(name = "offer_prices", joinColumns = @JoinColumn(name = "offer_id"))
    private List<PriceEntry> prices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "offer_approvals", joinColumns = @JoinColumn(name = "offer_id"))
    @Column(name = "user_id")
    private Set<String> approvedBy = new HashSet<>();

    protected Offer() {} // JPA needs a default constructor

    public Offer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails, SupplyDetails supplyDetails) {
        this.offerId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId = productId;
        this.paymentDetails = paymentDetails;
        this.supplyDetails = supplyDetails;
        this.counterOffer = false;
        this.isAccepted = false;

        this.prices.add(new PriceEntry(memberId, newPrice));
        this.approvedBy.add(memberId);
    }

    public boolean counterOffer(String userId, double newPrice) {
        this.approvedBy = new HashSet<>(List.of(userId));
        this.prices.add(new PriceEntry(userId, newPrice));
        this.counterOffer = !userId.equals(memberId);
        return counterOffer;
    }

    public void approveOffer(String userId) {
        if (!approvedBy.add(userId)) {
            throw new IllegalStateException("User " + userId + " has already approved this offer.");
        }
    }

    public boolean isCounterOffer() { return counterOffer; }
    public boolean isAccepted() { return isAccepted; }
    public void setAccepted(boolean accepted) { this.isAccepted = accepted; }

    public double getLastPrice() {
        if (prices.isEmpty()) {
            throw new IllegalStateException("No prices available for this offer.");
        }
        return prices.get(prices.size() - 1).getPrice();
    }

    public String getId() { return offerId; }
    public String getMemberId() { return memberId; }
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public List<Pair<String, Double>> getPrices() {
        List<Pair<String, Double>> result = new ArrayList<>();
        for (PriceEntry entry : prices) {
            result.add(new Pair<>(entry.getUserId(), entry.getPrice()));
        }
        return result;
    }   

    public Set<String> getApprovedBy() { return approvedBy; }
    public PaymentDetails getPaymentDetails() { return paymentDetails; }
    public SupplyDetails getSupplyDetails() { return supplyDetails; }
}
