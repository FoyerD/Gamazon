package Application;

import java.util.List;
import java.util.Map;

import Domain.Store.Item;
import Domain.Store.Store;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;



public class ItemService {
   public Response<Boolean> changePrice(int storeId, int productId, float newPrice){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>(false);
   }
   
   public Response<Map<Store, Item>> getItemsByProductId(Long productId){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>();
   }
   
   public Response<Item> getItem(String storeId, String productId){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>();
   }

   public Response<List<Item>> getItemsByStoreId(String storeId){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>();
   }

   public Response<List<Item>> getAvailableItems(String productId){
       try {
           throw new NotImplementedException("Not Implemented.");
       } catch (NotImplementedException ex) {
       }
       return new Response<>();
   }
}
