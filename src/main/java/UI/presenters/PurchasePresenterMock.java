package UI.presenters;

import Application.DTOs.OrderDTO;
import Application.utils.Response;
import Domain.Store.Category;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PurchasePresenterMock implements IPurchasePresenter {

    private final Map<String, Set<OrderDTO>> userCarts = new ConcurrentHashMap<>();

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

    private final Set<OrderDTO> defaultCartItems;

    public PurchasePresenterMock() {
        defaultCartItems = Set.of(
            new OrderDTO("prod1", "Smartphone", electronicsCategories, "TechStore", 1),
            new OrderDTO("prod2", "Speaker", electronicsCategories, "TechStore", 2),
            new OrderDTO("prod3", "T-Shirt", clothingCategories, "ClothingShop", 3),
            new OrderDTO("prod4", "Jeans", clothingCategories, "ClothingShop", 1),
            new OrderDTO("prod5", "Coffee Maker", homeCategories, "HomeGoods", 1),
            new OrderDTO("prod6", "Toaster", homeCategories, "HomeGoods", 1)
        );
    }

    @Override
    public Response<Boolean> addProductToCart(String sessionToken, String productId, String storeId, int amount) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);

        Optional<OrderDTO> existing = cart.stream()
                .filter(o -> o.getStoreName().equals(storeId))
                .findFirst();

        if (existing.isPresent()) {
            cart.remove(existing.get());
            OrderDTO updated = new OrderDTO(
                    productId,
                    existing.get().getName(),
                    existing.get().getCategories(),
                    storeId,
                    existing.get().getQuantity() + amount
            );
            cart.add(updated);
        } else {
            // Add new with fake category and name for simplicity
            Set<Category> categories = productId.contains("3") ? clothingCategories :
                    productId.contains("5") ? homeCategories : electronicsCategories;
            String name = "Mock Product " + productId;
            cart.add(new OrderDTO(productId, name, categories, storeId, amount));
        }

        return new Response<>(true);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);
        boolean removed = cart.removeIf(o -> o.getStoreName().equals(storeId));
        return new Response<>(removed);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId, int amount) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);

        Optional<OrderDTO> existing = cart.stream()
                .filter(o ->o.getStoreName().equals(storeId))
                .findFirst();

        if (existing.isPresent()) {
            OrderDTO current = existing.get();
            cart.remove(current);
            int remaining = current.getQuantity() - amount;
            if (remaining > 0) {
                OrderDTO updated = new OrderDTO(
                        current.getName(),
                        current.getCategories().stream().anyMatch(c -> c.getName().equals("Electronics")) ? 
                            "Electronic device" : (current.getCategories().stream().anyMatch(c -> c.getName().equals("Clothing")) ?
                            "Clothing item" : "Home item"),
                        current.getCategories(),
                        current.getStoreName(),
                        remaining
                );
                cart.add(updated);
            }
            return new Response<>(true);
        }

        return new Response<>(false);
    }

    @Override
    public Response<Set<OrderDTO>> viewCart(String sessionToken) {
        ensureCartExists(sessionToken);
        return new Response<>(new HashSet<>(userCarts.get(sessionToken)));
    }

    @Override
    public Response<Boolean> clearCart(String sessionToken) {
        userCarts.put(sessionToken, new HashSet<>());
        return new Response<>(true);
    }

    @Override
    public Response<Boolean> clearBasket(String sessionToken, String storeId) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);
        boolean changed = cart.removeIf(o -> o.getStoreName().equals(storeId));
        return new Response<>(changed);
    }

    @Override
    public Response<Boolean> makeBid(String sessionToken, String auctionId, float bid) {
        boolean success = bid > 0;
        return new Response<>(success);
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
        userCarts.putIfAbsent(sessionToken, new HashSet<>(defaultCartItems));
    }
}
