package Application;

import Domain.Store.Feedback;
import Domain.Store.StoreFacade;

public class CustomerServiceService {
    private StoreFacade storeFacade;
    
    public CustomerServiceService(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }
    public CustomerServiceService() {
        this.storeFacade = null;
    }
    public void setStoreFacade(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    public boolean isInitialized() {
        return this.storeFacade != null;

    public Response<Boolean> addFeedback(String customerId, String storeId, String productId, String comment) {
        try {
            // Assuming storeRepository has a method to add feedback
            boolean result = this.storeFacade.addFeedback(storeId, productId, customerId, comment);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<String> getFeedback(String customerId, String storeId, String productId) {
        try {
            Feedback feedback = this.storeFacade.getFeedback(storeId, productId, customerId);
            if (feedback != null) {
                return new Response<>(feedback.getComment());
            } else {
                throw new Exception("Feedback not found");
            }
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
