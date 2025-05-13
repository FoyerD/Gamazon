package UI.presenters;

import Application.DTOs.OrderDTO;
import Domain.Store.Category;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class PurchasePresenterMock implements IPurchasePresenter {

    // Map to store user carts: sessionToken -> List of OrderDTOs
    private final Map<String, Set<OrderDTO>> userCarts = new ConcurrentHashMap<>();
    
    // Known session tokens from LoginPresenterMock
    private static final String GUEST_TOKEN = "guest-token";
    private static final String REGISTERED_TOKEN = "fake-session-token";
    private static final String LOGIN_TOKEN = "mock-session-token";

    // Create mock categories for use in orders
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

    // Default cart data to be used for all users
    private final Set<OrderDTO> defaultCartItems;

    public PurchasePresenterMock() {
        // Initialize with mock data for easier testing
        defaultCartItems = new HashSet<>();
        defaultCartItems.add(new OrderDTO("Smartphone", "Latest model smartphone", electronicsCategories, "TechStore", 1));
        defaultCartItems.add(new OrderDTO("Bluetooth Speaker", "Wireless speaker with great sound", electronicsCategories, "TechStore", 2));
        defaultCartItems.add(new OrderDTO("T-Shirt", "Cotton T-shirt, size M", clothingCategories, "ClothingShop", 3));
        defaultCartItems.add(new OrderDTO("Jeans", "Blue denim jeans, size 32", clothingCategories, "ClothingShop", 1));
        defaultCartItems.add(new OrderDTO("Coffee Maker", "Automatic coffee machine", homeCategories, "HomeGoods", 1));
        defaultCartItems.add(new OrderDTO("Toaster", "4-slice toaster", homeCategories, "HomeGoods", 1));

        // Initialize carts for the known tokens
        Set<OrderDTO> guestCart = new HashSet<>(defaultCartItems);
        userCarts.put(GUEST_TOKEN, guestCart);
        
        Set<OrderDTO> registeredCart = new HashSet<>(defaultCartItems);
        // Add some special items for registered users
        registeredCart.add(new OrderDTO("Premium Headphones", "High-quality wireless headphones", 
                electronicsCategories, "TechStore", 1));
        userCarts.put(REGISTERED_TOKEN, registeredCart);
        
        Set<OrderDTO> loginCart = new HashSet<>(defaultCartItems);
        // Add some special items for logged-in users
        loginCart.add(new OrderDTO("Smart Watch", "Latest smartwatch model", 
                electronicsCategories, "TechStore", 1));
        userCarts.put(LOGIN_TOKEN, loginCart);
    }

    @Override
    public boolean addProductToCart(String sessionToken, String productName, String storeName, int amount) {
        // Create cart for the session if it doesn't exist
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);

        // Check if product already exists in the cart
        Optional<OrderDTO> existingProduct = cart.stream()
                .filter(order -> order.getName().equals(productName) && order.getStoreName().equals(storeName))
                .findFirst();

        if (existingProduct.isPresent()) {
            // Remove existing product
            cart.remove(existingProduct.get());
            // Add updated quantity (existing + new)
            OrderDTO updatedOrder = new OrderDTO(
                    productName,
                    existingProduct.get().getCategories().stream().anyMatch(c -> c.getName().equals("Electronics")) ? 
                        "Electronic device" : (existingProduct.get().getCategories().stream().anyMatch(c -> c.getName().equals("Clothing")) ?
                        "Clothing item" : "Home item"),
                    existingProduct.get().getCategories(),
                    storeName,
                    existingProduct.get().getQuantity() + amount
            );
            cart.add(updatedOrder);
        } else {
            // Add new product
            Set<Category> categories = electronicsCategories; // Default category
            String description = "Electronic device";
            
            if (productName.toLowerCase().contains("shirt") || productName.toLowerCase().contains("jeans")) {
                categories = clothingCategories;
                description = "Clothing item";
            } else if (productName.toLowerCase().contains("coffee") || productName.toLowerCase().contains("toaster")) {
                categories = homeCategories;
                description = "Home item";
            }
            
            OrderDTO newOrder = new OrderDTO(productName, description, categories, storeName, amount);
            cart.add(newOrder);
        }
        
        return true;
    }

    @Override
    public boolean removeProductFromCart(String sessionToken, String productName, String storeName) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);
        
        int initialSize = cart.size();
        cart.removeIf(order -> order.getName().equals(productName) && order.getStoreName().equals(storeName));
        
        return cart.size() < initialSize;
    }

    @Override
    public boolean removeProductFromCart(String sessionToken, String productName, String storeName, int amount) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);
        
        Optional<OrderDTO> existingProduct = cart.stream()
                .filter(order -> order.getName().equals(productName) && order.getStoreName().equals(storeName))
                .findFirst();
                
        if (existingProduct.isPresent()) {
            OrderDTO order = existingProduct.get();
            cart.remove(order);
            
            int newQuantity = order.getQuantity() - amount;
            if (newQuantity > 0) {
                OrderDTO updatedOrder = new OrderDTO(
                        order.getName(),
                        order.getCategories().stream().anyMatch(c -> c.getName().equals("Electronics")) ? 
                            "Electronic device" : (order.getCategories().stream().anyMatch(c -> c.getName().equals("Clothing")) ?
                            "Clothing item" : "Home item"),
                        order.getCategories(),
                        order.getStoreName(),
                        newQuantity
                );
                cart.add(updatedOrder);
            }
            
            return true;
        }
        
        return false;
    }

    @Override
    public Set<OrderDTO> viewCart(String sessionToken) {
        // If no cart exists for this session, create one with default items
        ensureCartExists(sessionToken);
        
        return new HashSet<>(userCarts.get(sessionToken));
    }

    @Override
    public boolean clearCart(String sessionToken) {
        userCarts.put(sessionToken, new HashSet<>());
        return true;
    }

    @Override
    public boolean clearBasket(String sessionToken, String storeName) {
        ensureCartExists(sessionToken);
        Set<OrderDTO> cart = userCarts.get(sessionToken);
        
        int initialSize = cart.size();
        cart.removeIf(order -> order.getStoreName().equals(storeName));
        
        return cart.size() < initialSize;
    }

    @Override
    public boolean makeBid(String sessionToken, String productName, String storeName, double bidAmount) {
        // Simplified mock implementation
        return bidAmount > 0;
    }

    @Override
    public boolean purchaseCart(String sessionToken, String paymentMethod, String address, String creditCardNumber, String expirationDate, String cvv) {
        ensureCartExists(sessionToken);
        
        // Simple validation for mock implementation
        boolean validPayment = paymentMethod != null && !paymentMethod.isEmpty();
        boolean validAddress = address != null && !address.isEmpty();
        boolean validCreditCard = creditCardNumber != null && creditCardNumber.length() >= 13;
        boolean validExpiration = expirationDate != null && expirationDate.contains("/");
        boolean validCvv = cvv != null && cvv.length() >= 3;
        
        if (validPayment && validAddress && validCreditCard && validExpiration && validCvv) {
            // Clear the cart upon successful purchase
            clearCart(sessionToken);
            return true;
        }
        
        return false;
    }
    
    // Helper method to ensure a cart exists for the session and populate it with default items if empty
    private void ensureCartExists(String sessionToken) {
        if (!userCarts.containsKey(sessionToken)) {
            // First check if this is a known token from LoginPresenterMock
            if (sessionToken.equals(GUEST_TOKEN) || sessionToken.equals(REGISTERED_TOKEN) || sessionToken.equals(LOGIN_TOKEN)) {
                // These should already be initialized in the constructor, but just in case
                Set<OrderDTO> newCart = new HashSet<>(defaultCartItems);
                userCarts.put(sessionToken, newCart);
            } else {
                // For unknown tokens, just create a cart with default items
                Set<OrderDTO> newCart = new HashSet<>(defaultCartItems);
                userCarts.put(sessionToken, newCart);
            }
        }
    }
} 