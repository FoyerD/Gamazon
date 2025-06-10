package Application.DTOs;

import java.util.HashMap;
import java.util.Map;

import Domain.Pair;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCart;


public class CartDTO {
    private String clientId;
    private Map<String, ShoppingBasketDTO> baskets; // maps from a store to a basket

    public CartDTO(String clientId, Map<String, ShoppingBasketDTO> baskets) {
        this.clientId = clientId;
        this.baskets = baskets != null ? baskets : new HashMap<>();
    }

    public CartDTO(IShoppingCart cart){
        this.clientId = cart.getClientId();
        this.baskets = new HashMap<>();
        for (String storeId : cart.getCart()) {
            ShoppingBasketDTO basket = new ShoppingBasketDTO(storeId, clientId, new HashMap<>());
            baskets.put(storeId, basket);
        }
    }

    public CartDTO(IShoppingCart cart, IShoppingBasketRepository basketRepository){
        this.clientId = cart.getClientId();
        this.baskets = new HashMap<>();
        for (String storeId : cart.getCart()) {
            ShoppingBasketDTO basket = new ShoppingBasketDTO(basketRepository.get(new Pair<>(clientId, storeId)));
            baskets.put(storeId, basket);
        }
    }

    public String getClientId() {
        return clientId;
    }

    public Map<String, ShoppingBasketDTO> getBaskets() {
        return baskets;
    }

    public ShoppingBasketDTO getBasket(String storeId) {
        return baskets.get(storeId);
    }


    public float getTotalPrice() {
        float total = 0;
        for (ShoppingBasketDTO basket : baskets.values()) {
            total += basket.getTotalPrice();
        }
        return total;
    }

    public float getPreDiscountPrice() {
        float total = 0;
        for (ShoppingBasketDTO basket : baskets.values()) {
            total += basket.getTotalOriginalPrice();
        }
        return total;
    }
    
}
