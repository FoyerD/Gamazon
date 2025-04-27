package Domain.Store;

import java.util.List;

import Domain.ILockbasedRepository;
import Domain.Pair;


public abstract class IFeedbackRepository extends ILockbasedRepository<Feedback, Pair<Pair<String, String>, String>> {
    public IFeedbackRepository() {
        super();
    }
    
    abstract public Feedback get(String storeId, String productId, String userId); 
    abstract public Feedback remove(String storeId, String productId, String userId);
    abstract public Feedback update(String storeId, String productId, String userId, Feedback item);
    abstract public boolean add(String storeId, String productId, String userId, Feedback item);  
    
    abstract public List<Feedback> getAllFeedbacksByStoreId(String storeId);
    abstract public List<Feedback> getAllFeedbacksByProductId(String productId);
    abstract public List<Feedback> getAllFeedbacksByUserId(String userId);
}
