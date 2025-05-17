package Infrastructure.Repositories;

import Domain.Store.IFeedbackRepository;
import Domain.Store.Feedback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MemoryFeedbackRepository extends IFeedbackRepository{
    private Map<String, Feedback> feedbacks;

    public MemoryFeedbackRepository() {
        super();
        this.feedbacks = new ConcurrentHashMap<>();
    }


    @Override
    public Feedback remove(String id) {
        if(!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        
        this.removeLock(id);
        return feedbacks.remove(id);
    }

    @Override
    public Feedback get(String id) {
        if(!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        return this.feedbacks.get(id);
    }

    @Override
    public Feedback update(String id, Feedback item) {
        if (!this.feedbacks.containsKey(id))
            throw new IllegalArgumentException("Item with this ID does not exist");
        if(!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        if (item == null)
            throw new IllegalArgumentException("Item cannot be null");
        if (!id.equals(item.getFeedbackId()))
            throw new IllegalArgumentException("ID does not match the feedback ID");
    
        return this.feedbacks.put(id, item);
    }

    @Override
    public boolean add(String id, Feedback item) {
        if (!this.isIdValid(id))
            throw new IllegalArgumentException("ID cannot be null");
        if (this.feedbacks.containsKey(id))
            throw new IllegalArgumentException("Item with this ID already exists");
        if (item == null)
            throw new IllegalArgumentException("Item cannot be null");
        if (!id.equals(item.getFeedbackId()))
            throw new IllegalArgumentException("ID does not match the feedback ID");
            
        this.addLock(id);
        return this.feedbacks.put(id, item) == null;
    }

    @Override
    public List<Feedback> getAllFeedbacksByStoreId(String storeId) {
        return this.feedbacks.values().stream()
                .filter(feedback -> feedback.getStoreId().equals(storeId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Feedback> getAllFeedbacksByProductId(String productId) {
        return this.feedbacks.values().stream()
                .filter(feedback -> feedback.getProductId().equals(productId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Feedback> getAllFeedbacksByUserId(String userId) {
        return this.feedbacks.values().stream()
                .filter(feedback -> feedback.getCustomerId().equals(userId))
                .collect(Collectors.toList());
    }
    
}
