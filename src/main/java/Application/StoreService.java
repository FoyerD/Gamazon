package Application;

import Domain.Store.IStoreRepository;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class StoreService {
    
    // public Response<Boolean> addItem(int storeId, int productId){
    //     try {
    //         throw new NotImplementedException("Not implemented.");
    //     } catch (NotImplementedException ex) {
    //     }
    //     return new Response<>(false);
    // }

    // public Response<Boolean> removeItem(int storeId, int productId){
    //     try {
    //         throw new NotImplementedException("Not implemented.");
    //     } catch (NotImplementedException ex) {
    //     }
    //     return new Response<>(false);
    // }

    IStoreRepository storeRepository;

    public StoreService() {
        this.storeRepository = null;
    }

    public StoreService(IStoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Response<Boolean> addStore(String name, String address, String description, String founderId) {
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }

    public Response<Boolean> closeStore(String storeId, String founderId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
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
