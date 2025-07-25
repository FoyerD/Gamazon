package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;
import jakarta.persistence.*;


@Entity
@Table(name = "max_price_condition")
public class MaxPriceCondition extends SimpleCondition{

    private double maxPrice;

    // Constructor for loading from repository with existing ID
    public MaxPriceCondition(String id, double maxPrice) {
        super(id);
        if(maxPrice <= 0) {
            throw new IllegalArgumentException("Max price cannot be negative");
        }
        this.maxPrice = maxPrice;
    }

    protected MaxPriceCondition() {
        super(); // Required by JPA
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket, BiFunction<String, String, Item> itemGetter) {
        if (shoppingBasket == null || shoppingBasket.getStoreId() == null) {
            throw new IllegalArgumentException("ShoppingBasket and StoreId cannot be null");
        }

        double totalBasketPrice = 0.0;
        
        // Calculate total price of entire basket
        for (String productId : shoppingBasket.getOrders().keySet()) {
            double unitPrice = itemGetter.apply(shoppingBasket.getStoreId(), productId).getPrice();
            int quantity = shoppingBasket.getQuantity(productId);
            totalBasketPrice += unitPrice * quantity;
        }
        
        return totalBasketPrice <= maxPrice;
    }

    // Getters for repository serialization
    public double getMaxPrice() {
        return maxPrice;
    }

}