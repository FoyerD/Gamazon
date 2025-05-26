package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

// This class represents a discount which is bounded by the maximum of multiple discounts.
// According to the fifth type of complext discount described in v2 ducument.

public class MaxDiscount extends CompositeDiscount {

    public MaxDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, discounts);

        if (discounts == null || discounts.isEmpty()) {
            throw new IllegalArgumentException("Discounts cannot be null or empty");
        }
        
        // No need for a composite condition here, as we are only interested in the maximum discount
        // across all provided discounts
        this.setCondition(new TrueCondition()); // No condition needed for max discount

    }

    public MaxDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, Set.of(discount1, discount2));
        if (discount1 == null || discount2 == null) {
            throw new IllegalArgumentException("Discounts cannot be null");
        }

        // No need for a composite condition here, as we are only interested in the maximum discount
        this.setCondition(new TrueCondition()); // No condition needed for max discount
    }

    
    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        
        Map<String, PriceBreakDown> output = new HashMap<>();
        Set<Map<String, PriceBreakDown>> toCompose = this.calculateAllSubDiscounts(basket);

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

    // checks if qualified by any of the discounts
    @Override
    public boolean isQualified(String productId) {
        for (Discount discount : discounts) {
            if (discount.isQualified(productId)) {
                return true; // If any discount qualifies, return true
            }
        }
        return false; // If none qualify, return false
    }

}
