package Domain.Store;

public class Feedback {
    private String customerId;
    private String storeId;
    private String productId;
    private String feedbackId;
    private String comment;

    public Feedback(String feedbackId, String customerId, String storeId, String productId, String comment) {
        this.feedbackId = feedbackId;
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
    public String getFeedbackId() {
        return feedbackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feedback)) return false;

        Feedback feedback = (Feedback) o;

        if (!customerId.equals(feedback.customerId)) return false;
        if (!storeId.equals(feedback.storeId)) return false;
        if (!productId.equals(feedback.productId)) return false;
        if (!feedbackId.equals(feedback.feedbackId)) return false;
        return comment.equals(feedback.comment);
    }
}
