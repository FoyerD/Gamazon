package Domain.Store.Discounts.Conditions;

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

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        int quantity = shoppingBasket.getQuantity(productId);
        return quantity >= minQuantity;
    }
    
}
