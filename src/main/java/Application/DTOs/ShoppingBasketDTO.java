package Application.DTOs;

import java.util.HashMap;
import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Discounts.ItemPriceBreakdown;

public class ShoppingBasketDTO {
    private String storeId;
    private String clientId;
    private String storeName;
    private Map<String, ItemDTO> orders;
    private Map<String, ItemPriceBreakdownDTO> priceBreakdowns;

    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders, String storeName) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
        this.storeName = storeName;
        this.priceBreakdowns = new HashMap<>();
    }
    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
        this.storeName = null;
        this.priceBreakdowns = new HashMap<>();
    }

    public ShoppingBasketDTO(ShoppingBasket basket) {
        this.storeId = basket.getStoreId();
        this.clientId = basket.getClientId();
        this.orders = new HashMap<>();
    }

    public ShoppingBasketDTO(String storeId, String clientId, Map<String, ItemDTO> orders, Map<String, ItemPriceBreakdownDTO> priceBreakdowns, String storeName) {
        this.storeId = storeId;
        this.clientId = clientId;
        this.orders = orders;
        this.storeName = storeName;
        this.priceBreakdowns = priceBreakdowns;
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

    public Map<String, ItemPriceBreakdownDTO> getPriceBreakdowns() {
        return priceBreakdowns;
    }
    public void setPriceBreakdowns(Map<String, ItemPriceBreakdownDTO> priceBreakdowns) {
        this.priceBreakdowns = priceBreakdowns;
    }
}
