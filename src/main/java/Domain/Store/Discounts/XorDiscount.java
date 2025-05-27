package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class XorDiscount extends CompositeDiscount {

    public XorDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, validateAndCreateSet(itemFacade, discount1, discount2));
    }

    // Constructor for loading from repository with existing UUID
    public XorDiscount(UUID id, ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(id, itemFacade, validateAndCreateSet(itemFacade, discount1, discount2), null);
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        
        Map<String, PriceBreakDown> output = new HashMap<>(); // Fixed: use HashMap instead of immutable Map.of()

        Discount discount1 = discounts.iterator().next();
        Discount discount2 = discounts.stream().skip(1).findFirst().orElse(null);

        Map<String, PriceBreakDown> subDiscount1 = discount1.calculatePrice(basket);
        Map<String, PriceBreakDown> subDiscount2 = (discount2 != null) ? discount2.calculatePrice(basket) : Map.of();

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Fixed XOR logic: only one of the discounts should apply
            PriceBreakDown breakdown1 = subDiscount1.get(productId);
            PriceBreakDown breakdown2 = subDiscount2.get(productId);
            
            boolean discount1Applies = breakdown1 != null && breakdown1.getDiscount() > 0;
            boolean discount2Applies = breakdown2 != null && breakdown2.getDiscount() > 0;
            
            if (discount1Applies && !discount2Applies) {
                output.put(productId, breakdown1);
            } else if (!discount1Applies && discount2Applies) {
                output.put(productId, breakdown2);
            } else {
                // If both or none apply, no discount
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
            }
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