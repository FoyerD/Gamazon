package UI.presenters;

import Application.DTOs.CategoryDTO;
import Application.DTOs.ItemDTO;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductPresenterMock implements IProductPresenter {

    private final Set<ItemDTO> mockProducts = Stream.of(
        // TechStore products
        new ItemDTO("TechStore", "prod1", 799.99, 10, "Latest model smartphone with high-res camera", Set.of(new CategoryDTO("Tech", "Tech gadgets"), new CategoryDTO("Electronics", "Electronic devices")), "Smartphone", 4.7),
        new ItemDTO("TechStore", "prod2", 129.99, 20, "Wireless speaker with great sound quality", Set.of(new CategoryDTO("Tech", "Tech gadgets"), new CategoryDTO("Electronics", "Electronic devices")), "Bluetooth Speaker", 4.5),
        
        // ClothingShop products
        new ItemDTO("ClothingShop", "prod3", 19.99, 50, "Cotton T-shirt, available in multiple sizes", Set.of(new CategoryDTO("Clothing", "Apparel"), new CategoryDTO("Fashion", "Fashion items")), "T-Shirt", 4.2),
        new ItemDTO("ClothingShop", "prod4", 49.99, 30, "Blue denim jeans, various sizes available", Set.of(new CategoryDTO("Clothing", "Apparel"), new CategoryDTO("Fashion", "Fashion items")), "Jeans", 4.3),
        
        // HomeGoods products
        new ItemDTO("HomeGoods", "prod5", 89.99, 15, "Automatic coffee machine for home use", Set.of(new CategoryDTO("Home", "Home goods"), new CategoryDTO("Kitchen", "Kitchen appliances")), "Coffee Maker", 4.6),
        new ItemDTO("HomeGoods", "prod6", 39.99, 25, "4-slice toaster with adjustable settings", Set.of(new CategoryDTO("Home", "Home goods"), new CategoryDTO("Kitchen", "Kitchen appliances")), "Toaster", 4.4),
        
        // Add some additional products
        new ItemDTO("ElectronicsStore", "prod7", 299.99, 5, "Wireless headphones with noise cancellation", Set.of(new CategoryDTO("Tech", "Tech gadgets")), "Headphones", 4.8),
        new ItemDTO("SportsShop", "prod8", 59.99, 30, "Running shoes for all terrains", Set.of(new CategoryDTO("Sports", "Sports equipment")), "Running Shoes", 4.4)
    ).collect(Collectors.toSet());

    @Override
    public Set<ItemDTO> showProductDetails(String sessionToken, String productName) {
        return mockProducts.stream()
                .filter(p -> p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public ItemDTO showProductDetailsOfaStore(String sessionToken, String productName, String storeName) {
        // Try to match by exact store ID and product name
        return mockProducts.stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName) && p.getStoreId().equalsIgnoreCase(storeName))
                .findFirst()
                // If not found, just match by product name (for cart integration)
                .orElse(mockProducts.stream()
                        .filter(p -> p.getProductName().equalsIgnoreCase(productName))
                        .findFirst()
                        .orElse(null));
    }

    @Override
    public Set<ItemDTO> showAllProducts(String sessionToken) {
        return mockProducts;
    }

    @Override
    public Set<ItemDTO> showProductsByCategories(String sessionToken, Set<String> categories) {
        return mockProducts.stream()
                .filter(p -> p.getCategories().stream()
                        .anyMatch(c -> categories.contains(c.getName())))
                .collect(Collectors.toSet());
    }

    @Override
    public void rateProduct(String sessionToken, String productName, String storeName, double rating, String feedback) {
        System.out.println("Mock: Rated product " + productName + " from " + storeName + " with rating " + rating);
    }

    @Override
    public Set<ItemDTO> showAuctionedProducts(String sessionToken) {
        // In this mock, returning all products as if they were auctioned
        return mockProducts;
    }

    @Override
    public Set<ItemDTO> showAuctionedProductsByCategories(String sessionToken, Set<String> categories) {
        return showProductsByCategories(sessionToken, categories); // Reuse logic
    }

    @Override
    public Set<ItemDTO> showAuctionedProduct(String sessionToken, String productName) {
        return showProductDetails(sessionToken, productName); // Reuse logic
    }
}
