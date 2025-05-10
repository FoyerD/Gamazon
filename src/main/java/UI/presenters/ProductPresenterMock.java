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
        new ItemDTO("store1", "prod1", 49.99, 20, "Bluetooth speaker", Set.of(new CategoryDTO("Tech", "Tech gadgets")), "Speaker", 4.6),
        new ItemDTO("store2", "prod2", 99.99, 5, "Wireless headphones", Set.of(new CategoryDTO("Tech", "Tech gadgets")), "Headphones", 4.8),
        new ItemDTO("store3", "prod3", 299.99, 2, "Smartwatch", Set.of(new CategoryDTO("Wearables", "Smart accessories")), "Watch", 4.2)
    ).collect(Collectors.toSet());

    @Override
    public Set<ItemDTO> showProductDetails(String sessionToken, String productName) {
        return mockProducts.stream()
                .filter(p -> p.getProductName().toLowerCase().contains(productName.toLowerCase()))
                .collect(Collectors.toSet());
    }

    @Override
    public ItemDTO showProductDetailsOfaStore(String sessionToken, String productName, String storeName) {
        return mockProducts.stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(productName) && p.getStoreId().equalsIgnoreCase(storeName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Set<ItemDTO> showAllProducts(String sessionToken) {
        return mockProducts;
    }

    @Override
    public Set<ItemDTO> showProductsByCategories(String sessionToken, Set<String> categories) {
        return mockProducts.stream()
                .filter(p -> p.getCategories().stream().anyMatch(c -> categories.contains(c.getName())))
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
