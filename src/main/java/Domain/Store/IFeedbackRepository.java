package Domain.Store;

import java.util.List;

import Domain.ILockbasedRepository;


public abstract class IFeedbackRepository extends ILockbasedRepository<Feedback, String> {
    public IFeedbackRepository() {
        super();
    }
    
    abstract public List<Feedback> getAllFeedbacksByStoreId(String storeId);
    abstract public List<Feedback> getAllFeedbacksByProductId(String productId);
    abstract public List<Feedback> getAllFeedbacksByUserId(String userId);
}
