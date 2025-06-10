package Domain.Shopping;

import java.util.UUID;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Offer {

    @Id
    private String offerId;

    private String memberId;
    private String storeId;
    private String productId;
    private double newPrice;

    @Embedded
    private PaymentDetails paymentDetails;

    protected Offer() {} // JPA needs a default constructor

    public Offer(String memberId, String storeId, String productId, double newPrice, PaymentDetails paymentDetails) {
        this.offerId = UUID.randomUUID().toString();
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId = productId;
        this.newPrice = newPrice;
        this.paymentDetails = paymentDetails;
    }

    public String getId() { return offerId; }
    public String getMemberId() { return memberId; }
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public double getNewPrice() { return newPrice; }
    public PaymentDetails getPaymentDetails() { return this.paymentDetails; }
}
