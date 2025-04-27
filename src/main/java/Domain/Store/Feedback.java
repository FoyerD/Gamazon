package Domain.Store;

import Domain.Pair;

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

    public static Pair<Pair<String, String>, String> getPairKey(String storeId, String productId, String customerId) {
        return new Pair<>(new Pair<>(storeId, productId), customerId);
    }
    public Pair<Pair<String, String>, String> getPairKey() {
        return new Pair<>(new Pair<>(this.getStoreId(), this.getProductId()), this.getCustomerId());
    }
}
