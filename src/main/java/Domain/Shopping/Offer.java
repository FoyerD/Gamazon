package Domain.Shopping;

import java.util.UUID;

public class Offer {
    private final UUID offerId;
    private final String memberId;
    private final String storeId;
    private final String productId;

    private final double newPrice;


    public Offer(String memberId, String storeId, String productId, double newPrice) {
        this.offerId = UUID.randomUUID();
        this.memberId = memberId;
        this.storeId = storeId;
        this.productId = productId;
        this.newPrice = newPrice;
        
    }

    public String getId() { return offerId.toString(); }
    public String getMemberId() { return memberId; }
    public String getStoreId() { return storeId; }
    public String getProductId() { return productId; }
    public double getNewPrice() { return newPrice; }
}
