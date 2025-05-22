package UI.presenters;

import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.utils.Response;
import Domain.Store.Category;
import Domain.Store.Item;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PurchasePresenterMock implements IPurchasePresenter {

    private final Map<String, CartDTO> userCarts = new ConcurrentHashMap<>();

    private final Set<Category> electronicsCategories = Set.of(
            new Category("Electronics", "Electronic devices and gadgets"),
            new Category("Tech", "Technology products")
    );

    private final Set<Category> clothingCategories = Set.of(
            new Category("Clothing", "Apparel and accessories"),
            new Category("Fashion", "Fashion items")
    );

    private final Set<Category> homeCategories = Set.of(
            new Category("Home", "Home and garden items"),
            new Category("Kitchen", "Kitchen appliances and tools")
    );

    // private final Set<OrderDTO> defaultCartItems;

    // public PurchasePresenterMock() {
    //     defaultCartItems = Set.of(
    //         new OrderDTO("prod1", "Smartphone", electronicsCategories, "TechStore", 1),
    //         new OrderDTO("prod2", "Speaker", electronicsCategories, "TechStore", 2),
    //         new OrderDTO("prod3", "T-Shirt", clothingCategories, "ClothingShop", 3),
    //         new OrderDTO("prod4", "Jeans", clothingCategories, "ClothingShop", 1),
    //         new OrderDTO("prod5", "Coffee Maker", homeCategories, "HomeGoods", 1),
    //         new OrderDTO("prod6", "Toaster", homeCategories, "HomeGoods", 1)
    //     );
    // }

    @Override
    public Response<Boolean> addProductToCart(String sessionToken, String productId, String storeId, int amount) {
        ensureCartExists(sessionToken);
        CartDTO cart = userCarts.get(sessionToken);
        Map<String, ShoppingBasketDTO> orders = cart.getBaskets();
        ShoppingBasketDTO basket = orders.get(storeId);
        Map<String, ItemDTO> items = basket.getOrders();
        //items.add(productId);
        return Response.success(true);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId) {
        ensureCartExists(sessionToken);
        CartDTO cart = userCarts.get(sessionToken);
        Map<String, ShoppingBasketDTO> orders = cart.getBaskets();
        ShoppingBasketDTO basket = orders.get(storeId);
        Map<String, ItemDTO> items = basket.getOrders();
        items.remove(productId);
        
        return Response.success(true);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId, int amount) {
        ensureCartExists(sessionToken);
                CartDTO cart = userCarts.get(sessionToken);
        Map<String, ShoppingBasketDTO> orders = cart.getBaskets();
        ShoppingBasketDTO basket = orders.get(storeId);
        Map<String, ItemDTO> items = basket.getOrders();
        ItemDTO item = items.get(productId);
        item.setAmount(amount);
        return Response.success(true);
    }

    // public Response<Set<OrderDTO>> viewCart(String sessionToken) {
    //     ensureCartExists(sessionToken);
    //     return new Response<>(new HashSet<>(userCarts.get(sessionToken)));
    // }
    @Override
    public Response<CartDTO> viewCart(String sessionToken) {
        ensureCartExists(sessionToken);
        return new Response<>(userCarts.get(sessionToken));
    }


    @Override
    public Response<Boolean> clearCart(String sessionToken) {
        userCarts.put(sessionToken, new CartDTO(sessionToken, new HashMap<>()));
        return new Response<>(true);
    }

    @Override
    public Response<Boolean> clearBasket(String sessionToken, String storeId) {
        ensureCartExists(sessionToken);
        CartDTO cart = userCarts.get(sessionToken);
        cart.getBaskets().remove(storeId);
        return Response.success(true);
    }

    
    public Response<Boolean> makeBid(String sessionToken, String auctionId, float bid) {
        boolean success = bid > 0;
        return new Response<>(success);
    }

    @Override
    public Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress) {
        throw new UnsupportedOperationException("Not relevant in mock");
        // boolean valid = cardNumber != null && !cardNumber.isEmpty()
        //              && expiryDate != null
        //              && cvv != null && cvv.length() >= 3;

        // if (valid) {
        //     return new Response<>(true);
        // }

        // return new Response<>(false);
    }

    @Override
    public Response<Boolean> purchaseCart(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement, String clientName, String deliveryAddress) {
        boolean valid = cardNumber != null && !cardNumber.isEmpty()
                     && expiryDate != null
                     && cvv != null && cvv.length() >= 3
                     && clientName != null && !clientName.isEmpty()
                     && deliveryAddress != null && !deliveryAddress.isEmpty();

        if (valid) {
            clearCart(sessionToken);
            return new Response<>(true);
        }

        return new Response<>(false);
    }

    private void ensureCartExists(String sessionToken) {
        userCarts.putIfAbsent(sessionToken, new CartDTO(sessionToken, new HashMap<>()));
    }
}
