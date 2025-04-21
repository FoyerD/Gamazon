package Domain.Store;

public class FeedbackFacade {
    private IFeedbackRepository feedbackRepository;
    
    public FeedbackFacade(IFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public FeedbackFacade() {
        this.feedbackRepository = null;
    }

    public void setFeedbackRepository(IFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback getFeedback(String storeId, String productId, String userId) {
        assert this.feedbackRepository != null : "Feedback repository is not initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";

        return feedbackRepository.get(storeId, productId, userId);
    }

    public boolean addFeedback(String storeId, String productId, String userId, String comment) {
        assert this.feedbackRepository != null : "Feedback repository is not initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";
        assert comment != null && !comment.isEmpty() : "Feedback cannot be null.";
        
        Feedback feedback = new Feedback(storeId, productId, userId, comment);
        return feedbackRepository.add(storeId, productId, userId, feedback);
    }

    public Feedback removeFeedback(String storeId, String productId, String userId) {
        assert this.feedbackRepository != null : "Feedback repository is not initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";

        return feedbackRepository.remove(storeId, productId, userId);
    }

    public Feedback updateFeedback(Feedback feedback) {
        assert this.feedbackRepository != null : "Feedback repository is not initialized.";
        assert feedback != null : "Feedback cannot be null.";
        assert feedback.getStoreId() != null && feedback.getProductId() != null && feedback.getCustomerId() != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !feedback.getStoreId().isEmpty() && !feedback.getProductId().isEmpty() && !feedback.getCustomerId().isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";
        assert feedback.getComment() != null && !feedback.getComment().isEmpty() : "Feedback cannot be null.";

        return feedbackRepository.update(feedback.getStoreId(), feedback.getProductId(), feedback.getCustomerId(), feedback);
    }
}
