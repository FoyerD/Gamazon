package Domain.Store;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    private String feedbackId;

    private String customerId;
    private String storeId;
    private String productId;
    private String comment;

    protected Feedback() {} // Required by JPA

    public Feedback(String feedbackId, String customerId, String storeId, String productId, String comment) {
        this.feedbackId = feedbackId;
        this.customerId = customerId;
        this.storeId = storeId;
        this.productId = productId;
        this.comment = comment;
    }

    public String getFeedbackId() {
        return feedbackId;
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

    // Setters for JPA
    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feedback)) return false;
        Feedback that = (Feedback) o;
        return feedbackId != null && feedbackId.equals(that.feedbackId);
    }

    @Override
    public int hashCode() {
        return feedbackId != null ? feedbackId.hashCode() : 0;
    }
}
