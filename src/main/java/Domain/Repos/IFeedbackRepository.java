package Domain.Repos;

import java.util.List;

import Domain.Store.Feedback;


public abstract class IFeedbackRepository extends ILockbasedRepository<Feedback, String> {
    public IFeedbackRepository() {
        super();
    }
    
    abstract public List<Feedback> getAllFeedbacksByStoreId(String storeId);
    abstract public List<Feedback> getAllFeedbacksByProductId(String productId);
    abstract public List<Feedback> getAllFeedbacksByUserId(String userId);
}
