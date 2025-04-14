package Domain.Store;

import java.util.List;

public class ItemFilter {
    private List<String> categories; 
    private Double minPrice;
    private Double maxPrice;
    private Long storeId; 
    private Boolean onlyAvailable; 
    private String productNameContains;
    
    public ItemFilter(){
        
    }
}
