package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MinQuantityCondition extends SimpleCondition{

    private String productId;
    private int minQuantity;


    // Constructor for loading from repository with existing ID
    public MinQuantityCondition(String id, String productId, int minQuantity) {
        super(id);
        this.productId = productId;
        this.minQuantity = minQuantity;
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