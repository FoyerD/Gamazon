package Domain.Store;

public class Feedback {
    private String customerId;
    private String storeId;
    private String productId;
    private String comment;

    public Feedback(String customerId, String storeId, String productId, String comment) {
        this.customerId = customerId;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
    }
    public String getCustomerId() {
        return customerId;
    }
    public String getStoreId() {
        return storeId;
    }
    public String getProductId() {
        return productId;
    }
    public String getComment() {
        return comment;
    }
}
