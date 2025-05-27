package Domain.Shopping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Domain.Store.ItemFacade;
import Domain.Store.Discounts.DiscountFacade;
import Domain.Store.Discounts.PriceBreakDown;
import Domain.Store.Discounts.Discount;

public class PriceCalculator {
    
    private ItemFacade itemFacade;
    private DiscountFacade discountFacade;

    public PriceCalculator(ItemFacade itemFacade, DiscountFacade discountFacade) {
        this.itemFacade = itemFacade;
        this.discountFacade = discountFacade;
    }


    // returns a Map from productId to PriceBreakDown
public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {

    Map<String, PriceBreakDown> output = new HashMap<>();
    
    if (basket == null) {
        throw new IllegalArgumentException("Shopping basket is null");
    }
    
    Set<Discount> storeDiscounts = discountFacade.getStoreDiscounts(basket.getStoreId());
    Set<Map<String, PriceBreakDown>> priceBreakDowns = new HashSet<>(); // Discounts to choose from

    // add all maps to the set
    for (Discount discount : storeDiscounts) {
        priceBreakDowns.add(discount.calculatePrice(basket));
    }

    // iterate over all products in basket, over all price breakdowns and choose the best.
    for (String productId : basket.getOrders().keySet()) {

        PriceBreakDown bestBreakDown = null;

        // choose best price breakdown
        for (Map<String, PriceBreakDown> priceBreakDown : priceBreakDowns) {
            PriceBreakDown currentBreakDown = priceBreakDown.get(productId);
            if (currentBreakDown != null) {
                if (bestBreakDown == null || currentBreakDown.getFinalPrice() < bestBreakDown.getFinalPrice()) {
                    bestBreakDown = currentBreakDown;
                }
            }
        }

        // If no discount was found for this product, create a breakdown with original price
        // Shouldn't happen but for safety
        if (bestBreakDown == null) {
            double originalPrice = itemFacade.getItem(basket.getStoreId(), productId).getPrice();
            bestBreakDown = new PriceBreakDown(originalPrice, 0.0); // 0% discount
        }

        output.put(productId, bestBreakDown);
    }

    return output;
}

}
