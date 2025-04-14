package Application;

import jdk.jshell.spi.ExecutionControl.NotImplementedException;



public class ItemService {
   public Response<Boolean> changePrice(int storeId, int productId, float newPrice){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>(false);
   }
}
