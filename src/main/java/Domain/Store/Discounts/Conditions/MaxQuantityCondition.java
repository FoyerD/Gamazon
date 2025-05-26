package Domain.Store.Discounts.Conditions;

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

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        int quantity = shoppingBasket.getQuantity(productId);
        return quantity <= maxQuantity;
    }

}
