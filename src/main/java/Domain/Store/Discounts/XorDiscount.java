package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class XorDiscount extends CompositeDiscount {


    // Assume there are only two sub-discounts for XOR. Easier Implementation.

    public XorDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, Set.of(discount1, discount2));
    }

    // public XorDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
    //     super(itemFacade, discounts);
    // }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        
        Map<String, PriceBreakDown> output = Map.of(); // Initialize output map

        // Get the two discounts
        Discount discount1 = discounts.iterator().next();
        Discount discount2 = discounts.stream().skip(1).findFirst().orElse(null);

        // Calculate price for each discount
        Map<String, PriceBreakDown> subDiscount1 = discount1.calculatePrice(basket);
        Map<String, PriceBreakDown> subDiscount2 = (discount2 != null) ? discount2.calculatePrice(basket) : Map.of();

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply XOR logic: only one of the discounts should apply
            if (subDiscount1.containsKey(productId) && !subDiscount2.containsKey(productId)) {
                output.put(productId, subDiscount1.get(productId));
            } else if (!subDiscount1.containsKey(productId) && subDiscount2.containsKey(productId)) {
                output.put(productId, subDiscount2.get(productId));
            } else {
                // If both or none apply, set to zero
                output.put(productId, subDiscount1.get(productId));
            }
        }

        return output;
    }

    // Checks if the product qualifies for any of the discounts
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
