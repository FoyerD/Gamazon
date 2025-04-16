package Application;

import Domain.Store.IStoreRepository;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class StoreService {

    private IStoreRepository storeRepository;

    public StoreService() {
        this.storeRepository = null;
    }

    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Response<Boolean> addStore(String sessionId, String name, String description) {
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }



    public Response<Boolean> openStore(String sessionId, String storeId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }

}
