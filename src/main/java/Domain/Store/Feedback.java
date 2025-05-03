package Domain.Store;

import Domain.Pair;

public class Feedback {
    private String customerId;
    private String storeId;
    private String productId;
    private String feeedbackId;
    private String comment;

    public Feedback(String feedbackId, String customerId, String storeId, String productId, String comment) {
        this.feeedbackId = feedbackId;
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
        return feeedbackId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feedback)) return false;

        Feedback feedback = (Feedback) o;

        if (!customerId.equals(feedback.customerId)) return false;
        if (!storeId.equals(feedback.storeId)) return false;
        if (!productId.equals(feedback.productId)) return false;
        if (!feeedbackId.equals(feedback.feeedbackId)) return false;
        return comment.equals(feedback.comment);
    }
}
