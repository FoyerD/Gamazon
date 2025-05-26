package Domain.Store.Discounts;

import java.util.HashMap;
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
        
        Map<String, PriceBreakDown> output = new HashMap<>();
        Set<Map<String, PriceBreakDown>> toCompose = this.calculateAllSubDiscounts(basket);

        
        // condition only applies if all discounts apply
        if (!conditionApplies(basket)) {
            for (String productId : basket.getOrders().keySet()) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
            }
            return output;
        }

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply the discount logic here, e.g., calculate the new price based on discountPercentage
            // This is a placeholder for actual price calculation logic

            // Apply the composition:
            double bestPercentage = 0;
            for (Map<String, PriceBreakDown> priceBreakDowns : toCompose) {
                if (priceBreakDowns.containsKey(productId)) {
                    PriceBreakDown priceBreakDown = priceBreakDowns.get(productId);
                    if (priceBreakDown.getDiscount() > bestPercentage) {
                        bestPercentage = priceBreakDown.getDiscount();
                    }
                }
            }

            PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), bestPercentage, null);
            output.put(productId, priceBreakDown);
        }

        return output;
    }

    @Override
    public boolean isQualified(String productId) {
        for (Discount discount : this.discounts) {
            if (!discount.isQualified(productId)) {
                return true;
            }
        }
        return false;
    }


    private boolean qualifiedByAll(String productId) {
        for (Discount discount : this.discounts) {
            if (!discount.isQualified(productId)) {
                return false;
            }
        }
        return true;
    }

}
