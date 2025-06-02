package Application.DTOs;

import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;

public class ShoppingBasketDTO {
    private String storeId;
    private String clientId;
    private String storeName;
    private Map<String, ItemDTO> orders;

    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders, String storeName) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
        this.storeName = storeName;
    }
    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
        this.storeName = null;
    }

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.clientId = basket.getClientId();
        this.orders = new HashMap<>();
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

    public float getTotalPrice() {
        float total = 0;
        for (ItemDTO item : orders.values()) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public float getTotalOriginalPrice() {
        float total = 0;
        for (ItemDTO item : orders.values()) {
            total += item.getTotalOriginalPrice();
        }
        return total;
    }
}
