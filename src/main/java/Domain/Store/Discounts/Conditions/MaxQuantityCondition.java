package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MaxQuantityCondition extends SimpleCondition {

    private String productId;
    private int maxQuantity;

    public MaxQuantityCondition(ItemFacade itemFacade, String productId, int maxQuantity) {
        super(itemFacade);
        this.productId = productId;
        this.maxQuantity = maxQuantity;
    }

    // Constructor for loading from repository with existing UUID
    public MaxQuantityCondition(String id, ItemFacade itemFacade, String productId, int maxQuantity) {
        super(id, itemFacade);
        this.productId = productId;
        this.maxQuantity = maxQuantity;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
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