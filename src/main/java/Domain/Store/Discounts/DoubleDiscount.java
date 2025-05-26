package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

// this class represents the combination of two discounts both are applied.
// according to the sixth type of complext discount described in v2 ducument.

public class DoubleDiscount extends CompositeDiscount {

    public DoubleDiscount(ItemFacade itemFacade,  Set<Discount> discounts) {
        super(itemFacade, discounts);

        if (discounts == null || itemFacade == null) {
            throw new IllegalArgumentException("Discounts and ItemFacade cannot be null");
        }

        // No need for a composite condition here, as we are only interested in applying both discounts
        this.setCondition(new TrueCondition()); // No condition needed for double discount
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        
        Set<Map<String, PriceBreakDown>> toCompose = this.calculateAllSubDiscounts(basket);
        Map<String, PriceBreakDown> output = new HashMap<>();


        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply the composition:
            double percentageAccumulator = 1;
            for (Map<String, PriceBreakDown> priceBreakDowns : toCompose) {
                if (priceBreakDowns.containsKey(productId)) {
                    PriceBreakDown priceBreakDown = priceBreakDowns.get(productId);
                    percentageAccumulator = percentageAccumulator * (1 - priceBreakDown.getDiscount());
                }
            }

            // Assuming the best percentage is applied to the original price
            double originalPrice = itemFacade.getItem(basket.getStoreId(), productId).getPrice();
            output.put(productId, new PriceBreakDown(originalPrice, percentageAccumulator, null));
        }

        return output;
    }

    // checks if qualified by any of the two discounts
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
