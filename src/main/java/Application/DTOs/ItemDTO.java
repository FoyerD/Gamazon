package Application.DTOs;

import java.util.Set;
import java.util.stream.Collectors;

import Domain.Store.Item;

public class ItemDTO {

    private final String storeId;
    private final String productId;
    private int amount;
    private final String description;
    private final Set<CategoryDTO> categories;
    private final String productName;
    private final double rating;
    private ItemPriceBreakdownDTO priceBreakDown; // Only needed for discounts when viewing the cart

    public ItemDTO(String storeId, String productId, ItemPriceBreakdownDTO priceBreakdown, int amount, String description, Set<CategoryDTO> categories, String productName, double rating) {
        this.storeId = storeId;
        this.productId = productId;
        this.amount = amount;
        this.description = description;
        this.categories = categories;
        this.productName = productName;
        this.rating = rating;
        this.priceBreakDown = null; // Only needed for discounts when viewing the cart
    }

    // Factory method
    public static ItemDTO fromItem(Item item) {
        return new ItemDTO(
            item.getStoreId(),
            item.getProductId(),
            new ItemPriceBreakdownDTO(item.getPrice(), 0),
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
        return priceBreakDown != null ? priceBreakDown.getFinalPrice() : 0.0;
    }

    public double getOriginalPrice() {
        return priceBreakDown != null ? priceBreakDown.getOriginalPrice() : 0.0;
    }

    public double getTotalPrice() {
        return getPrice() * amount;
    }
    
    public double getTotalOriginalPrice() {
        return getOriginalPrice() * amount;
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

    public ItemPriceBreakdownDTO getPriceBreakDown() {
        return priceBreakDown;
    }

    public void setPriceBreakDown(ItemPriceBreakdownDTO priceBreakDown) {
        // This method is not typically used, as priceBreakDown is set in the constructor
        // but can be used if needed for updates.
        this.priceBreakDown = priceBreakDown;
    }
}
