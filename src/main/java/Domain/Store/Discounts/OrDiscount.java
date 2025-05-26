package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.OrCondition;

public class OrDiscount extends CompositeDiscount {

    public OrDiscount(ItemFacade itemFacade, Discount discount, Set<Condition> conditions) {
        super(itemFacade, Set.of(discount));

        if (itemFacade == null || discount == null || conditions == null) {
            throw new IllegalArgumentException("ItemFacade, Discount, and Conditions cannot be null");
        }

        this.setCondition(new OrCondition(conditions));
    }

    // Constructor for loading from repository with existing UUID
    public OrDiscount(UUID id, ItemFacade itemFacade, Discount discount, Set<Condition> conditions) {
        super(id, itemFacade, Set.of(discount), new OrCondition(conditions));

        if (itemFacade == null || discount == null || conditions == null) {
            throw new IllegalArgumentException("ItemFacade, Discount, and Conditions cannot be null");
        }
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        Map<String, PriceBreakDown> output = new HashMap<>();

        Discount discount = this.discounts.iterator().next();
        Map<String, PriceBreakDown> subDiscounts = discount.calculatePrice(basket);

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            output.put(productId, subDiscounts.get(productId));
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