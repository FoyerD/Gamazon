package Domain.Store;

public class FeedbackDTO {
    private String customerId;
    private String storeId;
    private String productId;
    private String feedbackId;
    private String comment;

    public FeedbackDTO(String feedbackId, String customerId, String storeId, String productId, String comment) {
        this.feedbackId = feedbackId;
        this.customerId = customerId;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
    }

    public FeedbackDTO(Feedback feedback) {
        this.feedbackId = feedback.getFeedbackId();
        this.customerId = feedback.getCustomerId();
        this.storeId = feedback.getStoreId();
        this.productId = feedback.getProductId();
        this.comment = feedback.getComment();
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

    public String getFeedbackId() {
        return feedbackId;
    }
}
