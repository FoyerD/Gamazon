package Application;

import Domain.Store.IStoreRepository;

public class CustomerServiceService {
    private IStoreRepository storeRepository;
    
    public CustomerServiceService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }
    public CustomerServiceService() {
        this.storeRepository = null;
    }

    public Response<Boolean> setStoreRepository(IStoreRepository storeRepository) {
        try {
            return new Response<>();
        } catch (Exception ex) {
            return Response.error(ex.getMessage());
        }
    }

    public Response<Boolean> addFeedback(String customerId, String storeId, String productId, String comment) {
        try {
            // Assuming storeRepository has a method to add feedback
            return new Response<>();
        } catch (Exception ex) {
            return Response.error(ex.getMessage());
        }
    }
}
