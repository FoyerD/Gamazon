package Domain.Store;

import java.util.List;

import Domain.IRepository;
import Domain.Pair;


public interface IFeedbackRepository extends IRepository<Feedback, Pair<Pair<String, String>, String>> {
    public Feedback get(String storeId, String productId, String userId); 
    public Feedback remove(String storeId, String productId, String userId);
    public Feedback update(String storeId, String productId, String userId, Feedback item);
    public boolean add(String storeId, String productId, String userId, Feedback item);  
    
    public List<Feedback> getAllFeedbacksByStoreId(String storeId);
    public List<Feedback> getAllFeedbacksByProductId(String productId);
    public List<Feedback> getAllFeedbacksByUserId(String userId);
}
