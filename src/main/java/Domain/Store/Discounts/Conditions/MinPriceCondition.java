package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MinPriceCondition extends SimpleCondition {

    private double minPrice;

    public MinPriceCondition(ItemFacade itemFacade, double minPrice) {
        super(itemFacade);
        this.minPrice = minPrice;
    }

    // Constructor for loading from repository with existing UUID
    public MinPriceCondition(String id, ItemFacade itemFacade, double minPrice) {
        super(id, itemFacade);
        this.minPrice = minPrice;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
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