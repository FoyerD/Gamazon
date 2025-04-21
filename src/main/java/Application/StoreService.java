package Application;

import Domain.Store.Store;
import Domain.Store.StoreFacade;

public class StoreService {

    private StoreFacade storeFacade;

    public StoreService() {
        this.storeFacade = null;
    }

    public StoreService(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    public Response<Boolean> addStore(String sessionId, String name, String description) {
        try {
            if(this.storeFacade == null) return new Response<>(new Error("StoreFacade is not initialized."));
            
            //TODO!: session logic
            String userId = sessionId; // Assuming sessionId is the userId
            Store store = storeFacade.addStore(name, description, userId);
            return new Response<>(store != null);

        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }



    public Response<Boolean> openStore(String sessionId, String storeId){
        try {
            if(this.storeFacade == null) return new Response<>(new Error("StoreFacade is not initialized."));

            //TODO!: session logic
            boolean result = this.storeFacade.openStore(storeId);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
    public Response<Boolean> closeStore(String sessionId, String storeId){
        try {
            if(this.storeFacade == null) return new Response<>(new Error("StoreFacade is not initialized."));

            //TODO!: session logic
            boolean result = this.storeFacade.closeStore(storeId);
            return new Response<>(result);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
