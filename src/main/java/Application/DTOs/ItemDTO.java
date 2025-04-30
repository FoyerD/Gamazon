package Application.DTOs;

import java.util.Set;
import java.util.stream.Collectors;

import Domain.Store.Item;

public class ItemDTO {

    private final String storeId;
    private final String productId;
    private final double price;
    private final int amount;
    private final String description;
    private final Set<CategoryDTO> categories;
    private final String productName;
    private final double rating;

    public ItemDTO(String storeId, String productId, double price, int amount, String description, Set<CategoryDTO> categories, String productName, double rating) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.categories = categories;
        this.productName = productName;
        this.rating = rating;
    }

    // ✅ Factory method
    public static ItemDTO fromItem(Item item) {
        return new ItemDTO(
            item.getStoreId(),
            item.getProductId(),
            item.getPrice(),
            item.getAmount(),
            item.getDescription(),
            item.getCategories().stream()
                .map(CategoryDTO::fromCategory)
                .collect(Collectors.toSet()),
            item.getProductName(),
            item.getRating()
        );
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Set<CategoryDTO> getCategories() {
        return categories;
    }

    public String getProductName() {
        return productName;
    }

    public double getRating() {
        return rating;
    }
}
