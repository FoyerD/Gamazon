package Domain.Store.Discounts;

import java.util.Map;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.TrueCondition;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class SimpleDiscount extends Discount {

    private DiscountQualifier qualifier;
    private double discountPercentage; // INV: between 0 and 1 (percentage)

    public SimpleDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Condition condition) {
        super(itemFacade, condition);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
    }

    public SimpleDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier) {
        super(itemFacade);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
        this.setCondition(new TrueCondition());
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }

    @Override
    public boolean isQualified(String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isQualified'");
    }

}
