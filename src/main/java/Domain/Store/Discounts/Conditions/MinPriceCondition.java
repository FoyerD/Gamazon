package Domain.Store.Discounts.Conditions;

import java.util.function.BiFunction;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.Item;

public class MinPriceCondition extends SimpleCondition {

    private double minPrice;

    public MinPriceCondition(double minPrice) {
        super();
        this.minPrice = minPrice;
    }

    // Constructor for loading from repository with existing ID
    public MinPriceCondition(String id, double minPrice) {
        super(id);
        this.minPrice = minPrice;
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