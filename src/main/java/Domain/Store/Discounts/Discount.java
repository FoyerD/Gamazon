package Domain.Store.Discounts;

import java.util.Map;
import java.util.concurrent.locks.Condition;


import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public abstract class Discount {

    protected ItemFacade itemFacade;

    protected float discountPercentage; // INV: between 0 and 1
    protected DiscountQualifier qualifier;
    protected Condition condition; 


    public Discount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier) {
        this.itemFacade = itemFacade;
        this.discountPercentage = discountPercentage;
        this.qualifier = qualifier;
    }

    
    public float getDiscountPercentage() {
        return discountPercentage;
    }


    // Outputs a map from product to the price breakdown
    public abstract Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket);

    
    public boolean isQualified(String productId){
        return qualifier.isQualified(itemFacade.getProduct(productId));
    }


}
