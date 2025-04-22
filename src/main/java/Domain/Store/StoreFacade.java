package Domain.Store;

import java.util.function.Function;
import Domain.User.IUserRepository;
import Domain.User.User;

public class StoreFacade {
    private IStoreRepository storeRepository;
    private IFeedbackRepository feedbackRepository;
    private IProductRepository productRepository;
    private Function<String, User> getUser;

    public StoreFacade(IStoreRepository storeRepository, IFeedbackRepository feedbackRepository, IProductRepository productRepository, IUserRepository userRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.feedbackRepository = feedbackRepository;
        this.getUser = userRepository::get;
    }

    public StoreFacade() {
        this.storeRepository = null;
        this.feedbackRepository = null;
        this.productRepository = null;
        this.getUser = null;
    }

    public void setStoreRepository(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public void setFeedbackRepository(IFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void setProductRepository(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void setGetUser(IUserRepository userRepository) {
        this.getUser = userRepository::get;
    }

    public boolean isInitialized() {
        return this.storeRepository != null && this.feedbackRepository != null && this.productRepository != null && this.getUser != null;
    }

    public Store getStore(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        return storeRepository.get(storeId);
    }

    public Store addStore(String name, String description, String founderId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.getStoreByName(name) != null) throw new RuntimeException("Store name already exists.");
        if (this.getUser.apply(founderId) == null) throw new RuntimeException("User not found.");

        String storeId = System.currentTimeMillis() + "";
        Store store = new Store(storeId, name, description, founderId);
        this.storeRepository.add(storeId, store);
        return store;
    }

    public Store getStoreByName(String name) {
        return storeRepository.getStoreByName(name);
    }

    public boolean openStore(String storeId) {
        Store store = this.storeRepository.get(storeId);
        if (store == null) throw new RuntimeException("Store not found.");
        if (store.isOpen()) throw new RuntimeException("Store is already open.");

        store.setOpen(true);
        Store newStore = this.storeRepository.update(storeId, store);
        return store.equals(newStore);
    }

    public Feedback getFeedback(String storeId, String productId, String userId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        if (this.productRepository.get(productId) == null) throw new RuntimeException("Product not found.");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found.");

        return feedbackRepository.get(storeId, productId, userId);
    }

    public boolean addFeedback(String storeId, String productId, String userId, String comment) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        if (this.productRepository.get(productId) == null) throw new RuntimeException("Product not found.");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found.");

        Feedback feedback = new Feedback(storeId, productId, userId, comment);
        return feedbackRepository.add(storeId, productId, userId, feedback);
    }

    public Feedback removeFeedback(String storeId, String productId, String userId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");

        return feedbackRepository.remove(storeId, productId, userId);
    }

    public Feedback updateFeedback(Feedback feedback) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (feedback == null) throw new IllegalArgumentException("Feedback cannot be null.");

        return feedbackRepository.update(feedback.getStoreId(), feedback.getProductId(), feedback.getCustomerId(), feedback);
    }

    public boolean closeStore(String storeId){
        Store store = this.storeRepository.get(storeId);
        if (store == null) throw new RuntimeException("Store not found.");
        if(!store.isOpen()) throw new RuntimeException("Store is already closed.");

        store.setOpen(false);
        Store newStore = this.storeRepository.update(storeId, store);
        if(!store.equals(newStore)) throw new RuntimeException("Store not updated.");
        return true;
    }
}
