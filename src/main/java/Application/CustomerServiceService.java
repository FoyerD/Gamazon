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

    public Response<Void> setStoreRepository(IStoreRepository storeRepository) {
        try {
            return new Response<>();
        } catch (Exception ex) {
            return new Response<>(ex.getMessage());
        }
    }

    


    
}
