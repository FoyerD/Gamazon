package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

public class MaxDiscount extends CompositeDiscount {

    public MaxDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, discounts);

        if (discounts == null || discounts.isEmpty()) {
            throw new IllegalArgumentException("Discounts cannot be null or empty");
        }
        
        this.setCondition(new TrueCondition());
    }

    public MaxDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, validateAndCreateSet(itemFacade, discount1, discount2));

        if (discount1 == null || discount2 == null) {
            throw new IllegalArgumentException("Discounts cannot be null");
        }

        if (itemFacade == null) {
            throw new IllegalArgumentException("ItemFacade cannot be null");
        }

        this.setCondition(new TrueCondition());
    }

    // Constructor for loading from repository with existing UUID
    public MaxDiscount(UUID id, ItemFacade itemFacade, Set<Discount> discounts) {
        super(id, itemFacade, discounts, new TrueCondition());

        if (discounts == null || discounts.isEmpty()) {
            throw new IllegalArgumentException("Discounts cannot be null or empty");
        }
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
        for (Discount discount : discounts) {
            if (discount.isQualified(productId)) {
                return true;
            }
        }
        return false;
    }
}