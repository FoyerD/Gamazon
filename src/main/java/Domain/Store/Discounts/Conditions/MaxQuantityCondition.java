package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MaxQuantityCondition extends SimpleCondition {

    private String productId;
    private int maxQuantity;

    public MaxQuantityCondition(String productId, int maxQuantity) {
        super();
        this.productId = productId;
        this.maxQuantity = maxQuantity;
    }

    // Constructor for loading from repository with existing ID
    public MaxQuantityCondition(String id, String productId, int maxQuantity) {
        super(id);
        this.productId = productId;
        this.maxQuantity = maxQuantity;
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