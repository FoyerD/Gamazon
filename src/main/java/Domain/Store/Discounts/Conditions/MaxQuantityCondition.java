package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;


@Entity
@Table(name = "max_quantity_condition")
public class MaxQuantityCondition extends SimpleCondition {

    private String productId;
    private int maxQuantity;

    // Constructor for loading from repository with existing ID
    public MaxQuantityCondition(String id, String productId, int maxQuantity) {
        super(id);
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (maxQuantity <= 0) {
            throw new IllegalArgumentException("Max quantity must be greater than zero");
        }
        this.productId = productId;
        this.maxQuantity = maxQuantity;
    }

    protected MaxQuantityCondition() {
        super(); // JPA
    }
    
    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        int quantity = shoppingBasket.getQuantity(productId);
        return quantity <= maxQuantity;
    }

    // Getters for repository serialization
    public String getProductId() {
        return productId;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}