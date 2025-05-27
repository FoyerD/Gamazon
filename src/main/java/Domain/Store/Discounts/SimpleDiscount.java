package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Conditions.TrueCondition;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;

public class SimpleDiscount extends Discount {

    private DiscountQualifier qualifier;
    private float discountPercentage; // INV: between 0 and 1 (percentage)

    public SimpleDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Condition condition) {
        super(itemFacade, condition);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
    }

    public SimpleDiscount(ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier) {
        super(itemFacade);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
        this.setCondition(new TrueCondition());
    }

    // Constructor for loading from repository with existing UUID
    public SimpleDiscount(UUID id, ItemFacade itemFacade, float discountPercentage, DiscountQualifier qualifier, Condition condition) {
        super(id, itemFacade, condition);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        
        Map<String, PriceBreakDown> output = new HashMap<>();

        for (String productId : basket.getOrders().keySet()) {
            if (!isQualified(productId) || !conditionApplies(basket)) {
                PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), 0, null);
                output.put(productId, priceBreakDown);
                continue;
            }
            
            // Apply the discount logic here, e.g., calculate the new price based on discountPercentage
            // This is a placeholder for actual price calculation logic

            PriceBreakDown priceBreakDown = new PriceBreakDown(itemFacade.getItem(basket.getStoreId(), productId).getPrice(), this.discountPercentage, null);
            output.put(productId, priceBreakDown);
        }
        
        return output;
    }

    @Override
    public boolean isQualified(String productId) {
        return qualifier.isQualified(itemFacade.getProduct(productId));
    }

    // Getters for repository serialization
    public DiscountQualifier getQualifier() {
        return qualifier;
    }

    public float getDiscountPercentage() {
        return discountPercentage;
    }
}