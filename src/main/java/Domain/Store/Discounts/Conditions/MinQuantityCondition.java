package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "min_quantity_condition")
public class MinQuantityCondition extends SimpleCondition{

    private String productId;
    private int minQuantity;


    // Constructor for loading from repository with existing ID
    public MinQuantityCondition(String id, String productId, int minQuantity) {
        super(id);
        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (minQuantity <= 0) {
            throw new IllegalArgumentException("Min quantity must be greater than zero");
        }
        this.productId = productId;
        this.minQuantity = minQuantity;
    }

    protected MinQuantityCondition() {
        super(); // JPA
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        int quantity = shoppingBasket.getQuantity(productId);
        return quantity >= minQuantity;
    }

    // Getters for repository serialization
    public String getProductId() {
        return productId;
    }

    public int getMinQuantity() {
        return minQuantity;
    }
}