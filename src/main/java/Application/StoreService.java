package Application;

import Domain.Store.IStoreRepository;
import Domain.Store.Store;
import Domain.Store.StoreFacade;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

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
            
            //session logic
            String userId = sessionId; // Assuming sessionId is the userId
            Store store = storeFacade.addStore(name, description, userId);
            return new Response<>(store != null);

        } catch (Exception ex) {
        }
        return new Response<>(false);
    }



    public Response<Boolean> openStore(String storeId, String founderId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }

}
