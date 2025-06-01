package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

public class DoubleDiscount extends CompositeDiscount {

    public DoubleDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, discounts);

        if (discounts == null || itemFacade == null) {
            throw new IllegalArgumentException("Discounts and ItemFacade cannot be null");
        }

        this.setCondition(new TrueCondition());
    }

    // Constructor for loading from repository with existing UUID
    public DoubleDiscount(UUID id, ItemFacade itemFacade, Set<Discount> discounts) {
        super(id, itemFacade, discounts, new TrueCondition());

        if (discounts == null || itemFacade == null) {
            throw new IllegalArgumentException("Discounts and ItemFacade cannot be null");
        }
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket) {
        
        Set<Map<String, ItemPriceBreakdown>> toCompose = this.calculateAllSubDiscounts(basket);
        Map<String, ItemPriceBreakdown> output = new HashMap<>();

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                ItemPriceBreakdown priceBreakDown = new ItemPriceBreakdown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply the composition:
            double percentageAccumulator = 1;
            for (Map<String, ItemPriceBreakdown> priceBreakDowns : toCompose) {
                if (priceBreakDowns.containsKey(productId)) {
                    ItemPriceBreakdown priceBreakDown = priceBreakDowns.get(productId);
                    percentageAccumulator = percentageAccumulator * (1 - priceBreakDown.getDiscount());
                }
            }

            double originalPrice = itemFacade.getItem(basket.getStoreId(), productId).getPrice();
            // Fixed: store the actual discount percentage, not the remaining price ratio
            double finalDiscountPercentage = 1 - percentageAccumulator;
            output.put(productId, new ItemPriceBreakdown(originalPrice, finalDiscountPercentage, null));
        }

        return output;
    }

    @Override
    public boolean isQualified(String productId) {
        for (Discount discount : discounts) {
            if (discount.isQualified(productId)) {
                return true;
            }
        }
        return false;
    }
}