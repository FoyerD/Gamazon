package Domain.Store;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import Domain.User.IUserRepository;
import Domain.User.User;
import Domain.Pair;

public class StoreFacade {
    private IStoreRepository storeRepository;
    private IFeedbackRepository feedbackRepository;
    private IItemRepository itemRepository;
    private IAuctionRepository auctionRepository;
    private Function<String, User> getUser;

    public StoreFacade(IStoreRepository storeRepository, IFeedbackRepository feedbackRepository, IItemRepository itemRepository, IUserRepository userRepository, IAuctionRepository auctionRepository) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.feedbackRepository = feedbackRepository;
        this.auctionRepository = auctionRepository;
        this.getUser = userRepository::get;
    }

    public StoreFacade() {
        this.storeRepository = null;
        this.feedbackRepository = null;
        this.itemRepository = null;
        this.getUser = null;
        this.auctionRepository = null;
    }

    public void setStoreRepository(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    public void setAuctionRepository(IAuctionRepository auctionRepository) {
        this.auctionRepository = auctionRepository;
    }

    public void setFeedbackRepository(IFeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public void setItemRepository(IItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void setGetUser(IUserRepository userRepository) {
        this.getUser = userRepository::get;
    }

    public boolean isInitialized() {
        return this.storeRepository != null && this.feedbackRepository != null && this.itemRepository != null && this.getUser != null;
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
        if (!this.storeRepository.add(storeId, store)) throw new RuntimeException("Store not added.");
        return store;
    }

    public Store getStoreByName(String name) {
        return storeRepository.getStoreByName(name);
    }

    public boolean openStore(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");

        Object lock = this.storeRepository.getLock(storeId);
        if (lock == null) throw new RuntimeException("Store not found.");
        synchronized (lock) {
        
            Store store = this.storeRepository.get(storeId);
            if (store == null) throw new RuntimeException("Store not found.");
            if (store.isOpen()) throw new RuntimeException("Store is already open.");

            store.setOpen(true);
            Store newStore = this.storeRepository.update(storeId, store);
            return store.equals(newStore);
        }
    }

    public Feedback getFeedback(String storeId, String productId, String userId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null) throw new RuntimeException("Item not found.");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found.");

        return feedbackRepository.get(storeId, productId, userId);
    }

    public boolean addFeedback(String storeId, String productId, String userId, String comment) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null) throw new RuntimeException("Item not found.");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found.");

        Feedback feedback = new Feedback(storeId, productId, userId, comment);
        return feedbackRepository.add(storeId, productId, userId, feedback);
    }

    public Feedback removeFeedback(String storeId, String productId, String userId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");

        Object lock = this.feedbackRepository.getLock(Feedback.getPairKey(storeId, productId, userId));
        if (lock == null) throw new RuntimeException("Store not found.");
        synchronized (lock) {
            return feedbackRepository.remove(storeId, productId, userId);
        }
    }

    public Feedback updateFeedback(Feedback feedback) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (feedback == null) throw new IllegalArgumentException("Feedback cannot be null.");

        Object lock = this.feedbackRepository.getLock(feedback.getPairKey());
        if (lock == null) throw new RuntimeException("Store not found.");
        synchronized (lock) {
            return feedbackRepository.update(feedback.getStoreId(), feedback.getProductId(), feedback.getCustomerId(), feedback);
        }
    }

    public boolean closeStore(String storeId){
        Object lock = this.storeRepository.getLock(storeId);
        if (lock == null) throw new RuntimeException("Store not found.");
        synchronized (lock) {

            Store store = this.storeRepository.get(storeId);
            if (store == null) throw new RuntimeException("Store not found.");
            if(!store.isOpen()) throw new RuntimeException("Store is already closed.");

            store.setOpen(false);
            Store newStore = this.storeRepository.update(storeId, store);
            if(!store.equals(newStore)) throw new RuntimeException("Store not updated.");
            return true;
        }
    }

    public Auction addAuction(String storeId, String productId, String auctionEndDate, double startPrice) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null) throw new RuntimeException("Item not found.");
        
        Store store = this.storeRepository.get(storeId);
        if (!store.isOpen()) throw new RuntimeException("Store is not open.");

        Date auctionStartDate = new Date();
        Date auctionEndDateParsed = null;
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
        try {
            auctionEndDateParsed = parser.parse(auctionEndDate);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected format: EEE MMM d HH:mm:ss zzz yyyy");
        }
        
        if (auctionEndDateParsed.before(auctionStartDate)) throw new RuntimeException("Auction end date must be after the start date.");
        if (startPrice < 0) throw new RuntimeException("Start price must be greater than 0.");
        
        String auctionId = System.currentTimeMillis() + "";
        Auction auction = new Auction(auctionId, auctionStartDate, auctionEndDateParsed, startPrice, startPrice, storeId, productId);
        if (!this.auctionRepository.add(auctionId, auction)) throw new RuntimeException("Auction not added.");
        return auction;
        
    }

    public Auction getAuction(String auctionId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        return this.auctionRepository.get(auctionId);
    }

    public Auction addBid(String auctionId, String userId, float bid) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.auctionRepository.get(auctionId) == null) throw new RuntimeException("Auction not found.");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found.");
        if (bid < 0) throw new RuntimeException("Bid must be greater than 0.");

        Auction auction = this.auctionRepository.get(auctionId);
        if (bid <= auction.getCurrentPrice() || bid <= auction.getStartPrice()) throw new RuntimeException("Bid must be greater than current and start.");

        auction.setCurrentPrice(bid);
        auction.setCurrentBidderId(userId);
        return this.auctionRepository.update(auctionId, auction);
    }

    public Auction closeAuction(String auctionId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.auctionRepository.get(auctionId) == null) throw new RuntimeException("Auction not found.");

        return this.auctionRepository.remove(auctionId);
    }

    public List<Auction> getAllStoreAuctions(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found.");
        return this.auctionRepository.getAllStoreAuctions(storeId);
    }

    public List<Auction> getAllProductAuctions(String productId) {
        if (!isInitialized()) throw new RuntimeException("Facade must be initialized");
        return this.auctionRepository.getAllProductAuctions(productId);
    }
}
