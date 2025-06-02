package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.AndCondition;
import Domain.Store.Discounts.Conditions.Condition;

public class AndDiscount extends CompositeDiscount {

    public AndDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
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

    public AndDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, validateAndCreateSet(itemFacade, discount1, discount2));
        this.setCondition(new AndCondition(Set.of(discount1.getCondition(), discount2.getCondition())));
    }

    // Constructor for loading from repository with existing UUID
    public AndDiscount(UUID id, ItemFacade itemFacade, Set<Discount> discounts) {
        super(id, itemFacade, discounts, null);

        Set<Condition> conditions = new HashSet<>();
        for (Discount discount : discounts) {
            if (discount.getCondition() != null) {
                conditions.add(discount.getCondition());
            }
        }

        AndCondition compositeCondition = new AndCondition(conditions);
        this.setCondition(compositeCondition);
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket) {

        if (basket == null || basket.getStoreId() == null || basket.getOrders() == null) {
            throw new IllegalArgumentException("Basket, Store ID, and Orders cannot be null");
        }
        
        Map<String, ItemPriceBreakdown> output = new HashMap<>();
        
        // condition only applies if all discounts apply
        if (!conditionApplies(basket)) {
            for (String productId : basket.getOrders().keySet()) {
                ItemPriceBreakdown priceBreakDown = new ItemPriceBreakdown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
            }
            return output;
        }
        
        Set<Map<String, ItemPriceBreakdown>> toCompose = this.calculateAllSubDiscounts(basket);
        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                ItemPriceBreakdown priceBreakDown = new ItemPriceBreakdown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }

            // Apply the composition:
            double bestPercentage = 0;
            for (Map<String, ItemPriceBreakdown> priceBreakDowns : toCompose) {
                if (priceBreakDowns.containsKey(productId)) {
                    ItemPriceBreakdown priceBreakDown = priceBreakDowns.get(productId);
                    if (priceBreakDown.getDiscount() > bestPercentage) {
                        bestPercentage = priceBreakDown.getDiscount();
                    }
                }
            }

            ItemPriceBreakdown priceBreakDown = new ItemPriceBreakdown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), bestPercentage, null);
            output.put(productId, priceBreakDown);
        }

        return output;
    }

    // checks if any of the discounts is qualified
    @Override
    public boolean isQualified(String productId) {
        for (Discount discount : this.discounts) {
            if (discount.isQualified(productId)) {
                return true; // If ANY discount is qualified, return true
            }
        }
        return false; // If none qualify, return false
    }
}