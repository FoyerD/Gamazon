package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MinQuantityCondition extends SimpleCondition{

    private String productId;
    private int minQuantity;

    public MinQuantityCondition(ItemFacade itemFacade, String productId, int minQuantity) {
        super(itemFacade);
        this.productId = productId;
        this.minQuantity = minQuantity;
    }

    // Constructor for loading from repository with existing UUID
    public MinQuantityCondition(String id, ItemFacade itemFacade, String productId, int minQuantity) {
        super(id, itemFacade);
        this.productId = productId;
        this.minQuantity = minQuantity;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
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