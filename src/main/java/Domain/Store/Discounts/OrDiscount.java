package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.OrCondition;

public class OrDiscount extends CompositeDiscount {

    // public OrDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
    //     super(itemFacade, Set.of(discount1, discount2));
    //     Set<Condition> conditions = Set.of(discount1.getCondition(), discount2.getCondition());
    //     this.setCondition(new OrCondition(conditions));
    // }

    // public OrDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
    //     super(itemFacade, discounts);
    //     Set<Condition> conditions = new HashSet<>();
    //     for (Discount discount : discounts) {
    //         if (discount.getCondition() != null) {
    //             conditions.add(discount.getCondition());
    //         }
    //     }
    //     this.setCondition(new OrCondition(conditions));
    // }

    public OrDiscount(ItemFacade itemFacade, Discount discount, Set<Condition> conditions) {
        super(itemFacade, Set.of(discount));

        // check for null pointers
        if (itemFacade == null || discount == null || conditions == null) {
            throw new IllegalArgumentException("ItemFacade, Discount, and Conditions cannot be null");
        }

        this.setCondition(new OrCondition(conditions));
    }

    // Assumess that this one discount with an OR codition
    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        Map<String, PriceBreakDown> output = new HashMap<>();

        Discount discount = this.discounts.iterator().next(); // get the first discount
        Map<String, PriceBreakDown> subDiscounts = discount.calculatePrice(basket);

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply the composition. Assumes that there is one discount with an OR condition


            output.put(productId, subDiscounts.get(productId));
        }
     
        return output;
    }

    @Override
    public boolean isQualified(String productId) {
        for (Discount discount : discounts) {
            if (discount.isQualified(productId)) {
                return true; // If any discount qualifies, the OrDiscount qualifies
            }
        }
        return false; // If none of the discounts qualify, the OrDiscount does not qualify
    }

}
