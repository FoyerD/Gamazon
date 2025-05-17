package Application.DTOs;

import java.util.Map;

import Domain.Shopping.ShoppingBasket;

public class ShoppingBasketDTO {
    private String storeId;
    private String clientId;
    private Map<String, Integer> orders;

    public ShoppingBasketDTO(String storeId, String clientId, Map<String, Integer> orders) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
    }

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.clientId = basket.getClientId();
        this.orders = basket.getOrders();
    }

    public String getStoreId() {
        return storeId;
    }

    public Map<String, Integer> getOrders() {
        return orders;
    }

    public String getClientId() {
        return clientId;
    }
}
