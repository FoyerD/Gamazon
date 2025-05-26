package Domain.Store;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import Domain.User.IUserRepository;
import Domain.User.User;
import Domain.Pair;
import Domain.ExternalServices.INotificationService;
import Domain.Store.Discounts.Discount;



@Component
public class StoreFacade {
    private IStoreRepository storeRepository;
    private IFeedbackRepository feedbackRepository;
    private IItemRepository itemRepository;
    private IAuctionRepository auctionRepository;
    private Function<String, User> getUser;
    private INotificationService notificationService;


    @Autowired
    public StoreFacade(IStoreRepository storeRepository, IFeedbackRepository feedbackRepository, IItemRepository itemRepository, IUserRepository userRepository, IAuctionRepository auctionRepository, INotificationService notificationService) {
        this.itemRepository = itemRepository;
        this.storeRepository = storeRepository;
        this.feedbackRepository = feedbackRepository;
        this.auctionRepository = auctionRepository;
        this.getUser = userRepository::get;
        this.notificationService = notificationService;
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
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return storeRepository.get(storeId);
    }

    public Store addStore(String name, String description, String founderId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.getStoreByName(name) != null) throw new RuntimeException("Store name already exists");
        if (this.getUser.apply(founderId) == null) throw new RuntimeException("User not found");
        String storeId = UUID.randomUUID().toString();
        Store store = new Store(storeId, name, description, founderId);
        if (!this.storeRepository.add(storeId, store)) throw new RuntimeException("Store not added");
        return store;
    }

    public Store getStoreByName(String name) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return storeRepository.getStoreByName(name);
    }

    public boolean openStore(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");

        Object lock = this.storeRepository.getLock(storeId);
        if (lock == null) throw new RuntimeException("Store not found");
        synchronized (lock) {
        
            Store store = this.storeRepository.get(storeId);
            if (store == null) throw new RuntimeException("Store not found");
            if (store.isOpen()) throw new RuntimeException("Store is already open");

            store.setOpen(true);
            Store oldStore = this.storeRepository.update(storeId, store);
            return store.equals(oldStore);
        }
    }

    public Feedback getFeedback(String feedbackId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        
        return feedbackRepository.get(feedbackId);
    }

    public boolean addFeedback(String storeId, String productId, String userId, String comment) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found");
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null) throw new RuntimeException("Item not found");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found");
        if (comment == null || comment.isEmpty()) throw new RuntimeException("Comment cannot be null or empty");

        String feedbackId = UUID.randomUUID().toString();
        Feedback feedback = new Feedback(feedbackId, userId, storeId, productId, comment);
        return feedbackRepository.add(feedbackId, feedback);
    }

    public Feedback removeFeedback(String feedbackId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");

        Object lock = this.feedbackRepository.getLock(feedbackId);
        if (lock == null) throw new RuntimeException("Facade not found");
        synchronized (lock) {
            return feedbackRepository.remove(feedbackId);
        }
    }

    public List<Feedback> getAllFeedbacksByStoreId(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return feedbackRepository.getAllFeedbacksByStoreId(storeId);
    }
    public List<Feedback> getAllFeedbacksByProductId(String productId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return feedbackRepository.getAllFeedbacksByProductId(productId);
    }
    public List<Feedback> getAllFeedbacksByUserId(String userId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return feedbackRepository.getAllFeedbacksByUserId(userId);
    }

    public boolean closeStore(String storeId){
        Object lock = this.storeRepository.getLock(storeId);
        if (lock == null) throw new RuntimeException("Store not found");
        synchronized (lock) {

            Store store = this.storeRepository.get(storeId);
            if (store == null) throw new RuntimeException("Store not found");
            if(!store.isOpen()) throw new RuntimeException("Store is already closed");

            store.setOpen(false);
            store.setPermanentlyClosed(true);
            Store newStore = this.storeRepository.update(storeId, store);
            if(!store.equals(newStore)) throw new RuntimeException("Store not updated");
            return true;
        }
    }

    public boolean closeStoreNotPermanent(String storeId){
        Object lock = this.storeRepository.getLock(storeId);
        if (lock == null) throw new RuntimeException("Store not found");
        synchronized (lock) {

            Store store = this.storeRepository.get(storeId);
            if (store == null) throw new RuntimeException("Store not found");
            if(!store.isOpen()) throw new RuntimeException("Store is already closed");

            store.setOpen(false);
            store.setPermanentlyClosed(false);
            Store newStore = this.storeRepository.update(storeId, store);
            if(!store.equals(newStore)) throw new RuntimeException("Store not updated");
            return true;
        }
    }

    public Auction addAuction(String storeId, String productId, String auctionEndDate, double startPrice) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found");
        if (this.itemRepository.get(new Pair<>(storeId, productId)) == null) throw new RuntimeException("Item not found");
        
        Store store = this.storeRepository.get(storeId);
        if (!store.isOpen()) throw new RuntimeException("Store is not open");

        Date auctionStartDate = new Date();
        Date auctionEndDateParsed = null;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            auctionEndDateParsed = parser.parse(auctionEndDate);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format. Expected format: yyyy-MM-dd HH:mm");
        }
        
        if (auctionEndDateParsed.before(auctionStartDate)) throw new RuntimeException("Auction end date must be after the start date");
        if (startPrice < 0) throw new RuntimeException("Start price must be greater than 0");
        
        String auctionId = UUID.randomUUID().toString();
        Auction auction = new Auction(auctionId, auctionStartDate, auctionEndDateParsed, startPrice, startPrice, storeId, productId);
        if (!this.auctionRepository.add(auctionId, auction)) throw new RuntimeException("Auction not added");
        return auction;
        
    }

    public Auction getAuction(String auctionId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return this.auctionRepository.get(auctionId);
    }

    public String getStoreName(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        Store store = this.storeRepository.get(storeId);
        if (store == null) throw new RuntimeException("Store not found");
        return store.getName();
    }

    public Auction addBid(String auctionId, String userId, float bid, Supplier<Boolean> chargeCallback) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.auctionRepository.get(auctionId) == null) throw new RuntimeException("Auction not found");
        if (this.getUser.apply(userId) == null) throw new RuntimeException("User not found");
        if (bid < 0) throw new RuntimeException("Bid must be greater than 0");

        Auction auction = this.auctionRepository.get(auctionId);
        if (bid <= auction.getCurrentPrice() || bid <= auction.getStartPrice()) {
            throw new RuntimeException("Bid must be greater than current and start");
        }

        if (auction.currentBidderId != null)
            notificationService.sendNotification(auction.getCurrentBidderId(), "You have been outbid on auction " + auctionId + " womp womp :(");

        auction.setCurrentPrice(bid);
        auction.setCurrentBidderId(userId);
        auction.setChargeCallback(chargeCallback);

        return this.auctionRepository.update(auctionId, auction);
    }


    public Auction closeAuction(String auctionId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.auctionRepository.get(auctionId) == null) throw new RuntimeException("Auction not found");

        return this.auctionRepository.remove(auctionId);
    }

    public List<Auction> getAllStoreAuctions(String storeId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        if (this.storeRepository.get(storeId) == null) throw new RuntimeException("Store not found");
        return this.auctionRepository.getAllStoreAuctions(storeId);
    }

    public List<Auction> getAllProductAuctions(String productId) {
        if (!isInitialized()) throw new RuntimeException("Store facade must be initialized");
        return this.auctionRepository.getAllProductAuctions(productId);
    }

    public Item acceptBid(String storeId, String productId, String auctionId) {
        if (!isInitialized()) {
            throw new RuntimeException("StoreFacade is not initialized");
        }

        // Retrieve the item first and attempt to reserve one unit
        Pair<String, String> itemKey = new Pair<>(storeId, productId);
        Item item = itemRepository.get(itemKey);
        if (item == null) {
            throw new RuntimeException("Item not found for product " + productId + " in store " + storeId);
        }

        // Decrease the quantity (simulate reservation)
        int currentAmount = item.getAmount();
        if (currentAmount <= 0) {
            throw new RuntimeException("Insufficient item quantity to fulfill auction sale");
        }

        item.setAmount(currentAmount - 1);
        itemRepository.update(itemKey, item);

        // Retrieve the auction directly by ID
        Auction auction = this.auctionRepository.get(auctionId);
        if (auction == null) {
            // Rollback item amount
            item.setAmount(currentAmount);
            itemRepository.update(itemKey, item);
            throw new IllegalArgumentException("Auction not found with ID: " + auctionId);
        }

        // Sanity check: match storeId and productId
        if (!auction.getStoreId().equals(storeId) || !auction.getProductId().equals(productId)) {
            // Rollback item amount
            item.setAmount(currentAmount);
            itemRepository.update(itemKey, item);
            throw new IllegalArgumentException("Auction does not match provided store or product");
        }

        // Ensure there's a valid current bidder
        if (auction.getCurrentBidderId() == null) {
            // Rollback item amount
            item.setAmount(currentAmount);
            itemRepository.update(itemKey, item);
            throw new IllegalStateException("No bidder to accept the bid from");
        }

        // Charge the user using stored callback
        try {
            boolean success = auction.triggerCharge();
            if (!success) {
                // Rollback item amount
                item.setAmount(currentAmount);
                itemRepository.update(itemKey, item);
                throw new RuntimeException("Payment failed for accepted bid");
            }

        } catch (Exception ex) {
            // Rollback item amount
            item.setAmount(currentAmount);
            itemRepository.update(itemKey, item);
            throw new RuntimeException("Failed to charge the client for the accepted bid: " +  ex.getMessage(), ex);
        }

        notificationService.sendNotification(auction.getCurrentBidderId(), "🔔 🎉 You won the bid! in auction " + auctionId + " 🎉 🔔");
        // Final update: optionally mark buyer (if you have a field), or leave updated amount
        itemRepository.update(itemKey, item);

        // Remove the auction as it's now fulfilled
        this.auctionRepository.remove(auctionId);

        return item;
    }


    public boolean addDiscount(String storeId, Discount discount) {
        throw new UnsupportedOperationException("Method not implemented yet");
    }

}
