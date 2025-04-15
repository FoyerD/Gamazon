package Application;

import java.util.List;

import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.ItemFilter;



public class ItemService {
    
    private ItemFacade itemFacade;

    public ItemService(ItemFacade itemFacade){
        this.itemFacade = itemFacade;
    }

    public Response<Boolean> changePrice(String storeId, String productId, float newPrice){
        Response<Item> itemRes = getItem(storeId, productId);
        if(itemRes.errorOccurred())
            return new Response<>(new Error(itemRes.getErrorMessage()));
        try{
            itemRes.getValue().setPrice(newPrice);
            return new Response<>(true);
        }
        catch(Exception ex){
            return new Response<>(new Error(ex.getMessage()));
        }
   }
   
    public Response<List<Item>> getItemsByProductId(String productId){
        try {
            return new Response<>(itemFacade.getItemsProductId(productId));
        } 
        catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
   
    public Response<List<Item>> filterItems(ItemFilter filter){
        try {
            return new Response<>(itemFacade.filterItems(filter));
        } 
        catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        } 
   }

    public Response<Item> getItem(String storeId, String productId){
        try {
            return new Response<>(itemFacade.getItem(storeId, productId));
        }
        catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

   public Response<List<Item>> getItemsByStoreId(String storeId){
       try {
            return new Response<>(itemFacade.getItemsByStoreId(storeId));
        } 
        catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
   }

   public Response<List<Item>> getAvailableItems(String productId){
        try {
            return new Response<>(itemFacade.getAvailableItems());
        } 
        catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
   }
}
