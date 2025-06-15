package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;


@Entity
@Table(name = "min_price_condition")
public class MinPriceCondition extends SimpleCondition {

    private double minPrice;

    // Constructor for loading from repository with existing ID
    public MinPriceCondition(String id, double minPrice) {
        super(id);
        if (minPrice <= 0) {
            throw new IllegalArgumentException("Min price must be greater than zero");
        }
        this.minPrice = minPrice;
    }

    protected MinPriceCondition() {
        super(); // JPA
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        double totalBasketPrice = 0.0;
        
        // Calculate total price of entire basket
        for (String productId : shoppingBasket.getOrders().keySet()) {
            double unitPrice = itemGetter.apply(shoppingBasket.getStoreId(), productId).getPrice();
            int quantity = shoppingBasket.getQuantity(productId);
            totalBasketPrice += unitPrice * quantity;
        }
        
        return totalBasketPrice >= minPrice;
    }

    // Getters for repository serialization
    public double getMinPrice() {
        return minPrice;
    }

}