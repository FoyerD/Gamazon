package Application.DTOs;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import Domain.Shopping.ShoppingBasket;

public class ShoppingBasketDTO {
    private String storeId;
    private String clientId;
    private String storeName;
    private Map<String, ItemDTO> orders;

    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders, String storeName) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders != null ? orders : new HashMap<>();
        this.storeName = storeName;
    }
    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders != null ? orders : new HashMap<>();
        this.storeName = null;
    }

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.clientId = basket.getClientId();
        this.orders = new HashMap<>();
        
        // Copy orders from basket
        Map<String, Integer> basketOrders = basket.getOrders();
        if (basketOrders != null) {
            for (Map.Entry<String, Integer> entry : basketOrders.entrySet()) {
                String productId = entry.getKey();
                Integer quantity = entry.getValue();
                if (productId != null && quantity != null) {
                    // Create ItemDTO with required fields
                    ItemDTO itemDTO = new ItemDTO(
                        basket.getStoreId(),  // storeId
                        productId,            // productId
                        0.0,                  // price (will be updated when viewing)
                        quantity,             // amount
                        "",                   // description
                        new HashSet<>(),      // categories
                        "",                   // productName
                        0.0                   // rating
                    );
                    this.orders.put(productId, itemDTO);
                }
            }
        }
    }

    public String getStoreId() {
        return storeId;
    }

    public Map<String, ItemDTO> getOrders() {
        return orders;
    }

    public String getClientId() {
        return clientId;
    }

    public String getStoreName(){
        return storeName;
    }
}
