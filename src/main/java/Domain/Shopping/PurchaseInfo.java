package Domain.Shopping;

public class PurchaseInfo {
    private final String clientId;
    private final String storeId;
    private final String productId; 
    private final int quantity;
    private double bidPrice; // optional, relevant for bid/auction/lottery
    private String paymentDetails; // optional, relevant for immediate purchase

    public PurchaseInfo(String clientId, String storeId, String productId, int quantity) {
        this.clientId = clientId;
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and setters
    public String getClientId() {
        return clientId;
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }
}
