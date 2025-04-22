package Domain.Store;

public class StoreFacade {
    private IStoreRepository storeRepository;
    private IFeedbackRepository feedbackRepository;


    public StoreFacade(IStoreRepository storeRepository, IFeedbackRepository feedbackRepository) {
        this.storeRepository = storeRepository;
        this.feedbackRepository = feedbackRepository;
    }
    public StoreFacade() {
        this.storeRepository = null;
        this.feedbackRepository = null;
    }
    public void setStoreRepository(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    public void setFeedbackRepository(IFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public boolean isInitialized() {
        return this.storeRepository != null && this.feedbackRepository != null;
    }

    public Store getStore(String storeId) {
        return storeRepository.get(storeId);
    }
    public Store addStore(String name, String description, String founderId) {
        if (this.getStoreByName(name) != null) throw new RuntimeException("Store name already exists.");

        String storeId = System.currentTimeMillis() + "";
        Store store = new Store(storeId, name, description, founderId);
        this.storeRepository.add(storeId, store);
        return store;
    }
    public Store getStoreByName(String name) {
        return storeRepository.getStoreByName(name);
    }

    public boolean openStore(String storeId){
        Store store = this.storeRepository.get(storeId);
        if (store == null) throw new RuntimeException("Store not found.");
        if(store.isOpen()) throw new RuntimeException("Store is already open.");

        store.setOpen(true);
        Store newStore = this.storeRepository.update(storeId, store);
        if(!store.equals(newStore)) throw new RuntimeException("Store not updated.");
        return true;
    }



    public Feedback getFeedback(String storeId, String productId, String userId) {
        assert isInitialized() : "Store and feedback repositories must be initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";

        return feedbackRepository.get(storeId, productId, userId);
    }

    public boolean addFeedback(String storeId, String productId, String userId, String comment) {
        assert isInitialized() : "Store and feedback repositories must be initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";
        assert this.storeRepository.get(storeId) != null : "Store not found.";
        assert comment != null && !comment.isEmpty() : "Feedback cannot be null.";
        
        Feedback feedback = new Feedback(storeId, productId, userId, comment);
        return feedbackRepository.add(storeId, productId, userId, feedback);
    }

    public Feedback removeFeedback(String storeId, String productId, String userId) {
        assert isInitialized() : "Store and feedback repositories must be initialized.";
        assert storeId != null && productId != null && userId != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !storeId.isEmpty() && !productId.isEmpty() && !userId.isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";

        return feedbackRepository.remove(storeId, productId, userId);
    }

    public Feedback updateFeedback(Feedback feedback) {
        assert isInitialized() : "Store and feedback repositories must be initialized.";
        assert feedback != null : "Feedback cannot be null.";
        assert feedback.getStoreId() != null && feedback.getProductId() != null && feedback.getCustomerId() != null : "Store ID, Product ID, and User ID cannot be null.";
        assert !feedback.getStoreId().isEmpty() && !feedback.getProductId().isEmpty() && !feedback.getCustomerId().isEmpty() : "Store ID, Product ID, and User ID cannot be empty.";
        assert feedback.getComment() != null && !feedback.getComment().isEmpty() : "Feedback cannot be null.";

        return feedbackRepository.update(feedback.getStoreId(), feedback.getProductId(), feedback.getCustomerId(), feedback);
    }
}
