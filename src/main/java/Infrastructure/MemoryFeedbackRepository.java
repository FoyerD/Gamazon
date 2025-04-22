package Infrastructure;

import Domain.Store.IFeedbackRepository;
import Domain.Store.Feedback;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import Domain.Pair; 
public class MemoryFeedbackRepository implements IFeedbackRepository{
    private Map<Pair<Pair<String, String>, String>, Feedback> feedbacks;

    public MemoryFeedbackRepository() {
        this.feedbacks = new ConcurrentHashMap<>();
    }

    private Pair<Pair<String, String>, String> getId(Feedback feedback) {
        return new Pair<>(new Pair<>(feedback.getStoreId(), feedback.getProductId()), feedback.getCustomerId());
    }

    @Override
    public Feedback get(String storeId, String productId, String userId) {
        return this.get(new Pair<>(new Pair<>(storeId, productId), userId));
    }

    @Override
    public boolean add(String storeId, String productId, String userId, Feedback item) {
        return this.add(new Pair<>(new Pair<>(storeId, productId), userId), item);
    }

    @Override
    public Feedback remove(String storeId, String productId, String userId) {
        return this.remove(new Pair<>(new Pair<>(storeId, productId), userId));
    }

    public Feedback update(String storeId, String productId, String userId, Feedback item) {
        return this.update(new Pair<>(new Pair<>(storeId, productId), userId), item);
    }


    @Override
    public Feedback remove(Pair<Pair<String, String>, String> id) {
        return feedbacks.remove(id);
    }

    @Override
    public Feedback get(Pair<Pair<String, String>, String> id) {
        return this.feedbacks.get(id);
    }

    @Override
    public Feedback update(Pair<Pair<String, String>, String> id, Feedback item) {
        if (!this.feedbacks.containsKey(id)) throw new IllegalArgumentException("Item with this ID does not exist");
        if (!id.equals(this.getId(item))) {
            throw new IllegalArgumentException("ID does not match the feedback ID");
        }
        return this.feedbacks.put(id, item);
    }

    @Override
    public boolean add(Pair<Pair<String, String>, String> id, Feedback item) {
        if (this.feedbacks.containsKey(id)) throw new IllegalArgumentException("Item with this ID already exists");
        if (!id.equals(this.getId(item))) {
            throw new IllegalArgumentException("ID does not match the feedback ID");
        }
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
