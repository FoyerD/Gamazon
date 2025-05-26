package Domain.Store.Discounts.Conditions;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MaxPriceCondition extends SimpleCondition{

    private double maxPrice;
    private String productId;
    private String storeId;

    public MaxPriceCondition(ItemFacade itemFacade, String storeId, String productId, double maxPrice) {
        super(itemFacade);
        this.storeId = storeId;
        this.productId = productId;
        this.maxPrice = maxPrice;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        double unitPrice = itemFacade.getItem(storeId, productId).getPrice();
        double price = unitPrice * shoppingBasket.getQuantity(productId);
        return price <= maxPrice;
    }

}
