package Domain.Store.Discounts;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import Domain.Store.Discounts.Conditions.Condition;
import Domain.Store.Discounts.Qualifiers.DiscountQualifier;
import jakarta.persistence.*;

@Entity
@Table(name = "simple_discount")
public class SimpleDiscount extends Discount {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "qualifier_id")
    private DiscountQualifier qualifier;
    private double discountPercentage; // INV: between 0 and 1 (percentage)

    // Constructor for loading from repository with existing ID
    public SimpleDiscount(String id, String storeId, double discountPercentage, DiscountQualifier qualifier, Condition condition, String description) {
        super(id, storeId, condition);
        this.qualifier = qualifier;
        this.discountPercentage = discountPercentage;
    }

    public SimpleDiscount(String id, String storeId, double discountPercentage, DiscountQualifier qualifier, Condition condition) {
        this(id, storeId, discountPercentage, qualifier, condition, "Default Simple Discount Description");
    }

    @Override
    public Map<String, ItemPriceBreakdown> calculatePrice(ShoppingBasket basket, BiFunction<String, String, Item> itemGetter) {
        
        Map<String, ItemPriceBreakdown> output = new HashMap<>();
        double discountPercentageToApply = 0;
        if(!conditionApplies(basket, itemGetter)) {
            return basket.getBestPrice(itemGetter); // If condition does not apply, return original prices
        }
        for (String productId : basket.getOrders().keySet()) {
            discountPercentageToApply = 0;
            if (isQualified(basket.getStoreId(), productId, itemGetter) || !conditionApplies(basket, itemGetter)) {
                discountPercentageToApply = this.discountPercentage;
            }
            
            ItemPriceBreakdown priceBreakDown = new ItemPriceBreakdown(itemGetter.apply(basket.getStoreId(), productId).getPrice(), discountPercentageToApply, null);
            output.put(productId, priceBreakDown);
        }
        
        return output;
    }

    public boolean isQualified(String storeId, String productId, BiFunction<String, String, Item> itemGetter) {
        return qualifier.isQualified(itemGetter.apply(storeId, productId));
    }

    // Getters for repository serialization
    public DiscountQualifier getQualifier() {
        return qualifier;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }
}