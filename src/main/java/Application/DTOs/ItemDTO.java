package Application.DTOs;

import java.util.Set;
import java.util.stream.Collectors;

import Domain.Store.Item;

public class ItemDTO {

    private final String storeId;
    private final String productId;
    private final double price;
    private int amount;
    private final String description;
    private final Set<CategoryDTO> categories;
    private final String productName;
    private final double rating;
    private final PriceBreakDownDTO priceBreakDown; // Only needed for discounts when viewing the cart

    public ItemDTO(String storeId, String productId, double price, int amount, String description, Set<CategoryDTO> categories, String productName, double rating) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.categories = categories;
        this.productName = productName;
        this.rating = rating;
        this.priceBreakDown = null; // Only needed for discounts when viewing the cart
    }

    public ItemDTO(String storeId, String productId, double price, int amount, String description, Set<CategoryDTO> categories, String productName, double rating, PriceBreakDownDTO priceBreakDown) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
        this.categories = categories;
        this.productName = productName;
        this.rating = rating;
        this.priceBreakDown = priceBreakDown; // Only needed for discounts when viewing the cart
    }

    // Factory method
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

    public static ItemDTO fromItem(Item item, PriceBreakDownDTO priceBreakDown) {
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
            item.getRating(),
            priceBreakDown
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

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public PriceBreakDownDTO getPriceBreakDown() {
        return priceBreakDown;
    }
}
