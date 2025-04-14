package Application;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;

public class StoreService {
    
    public Response<Boolean> addItem(int storeId, int productId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }

    public Response<Boolean> removeItem(int storeId, int productId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }    

    public Response<Boolean> closeStore(int storeId){
        try {
            throw new NotImplementedException("Not implemented.");
        } catch (NotImplementedException ex) {
        }
        return new Response<>(false);
    }
    
}
