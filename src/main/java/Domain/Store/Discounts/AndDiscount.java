package Domain.Store.Discounts;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.AndCondition;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class AndDiscount extends CompositeDiscount {

    public AndDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Set<Discount> discounts) {
        super(itemFacade, discounts);

        Set<Condition> conditions = new HashSet<>();

        for (Discount discount : discounts) {
            if (discount.getCondition() != null) {
                conditions.add(discount.getCondition());
            }
        }

        AndCondition compositeCondition = new AndCondition(conditions);


        this.setCondition(compositeCondition);
    }

    public AndDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Discount discount1, Discount discount2) {
        super(itemFacade, Set.of(discount1, discount2));
        this.setCondition(new AndCondition(Set.of(discount1.getCondition(), discount2.getCondition())));
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
