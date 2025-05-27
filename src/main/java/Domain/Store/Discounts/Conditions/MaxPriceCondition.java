package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MaxPriceCondition extends SimpleCondition{

    private double maxPrice;

    public MaxPriceCondition(ItemFacade itemFacade, double maxPrice) {
        super(itemFacade);
        this.maxPrice = maxPrice;
    }

    // Constructor for loading from repository with existing UUID
    public MaxPriceCondition(UUID id, ItemFacade itemFacade, double maxPrice) {
        super(id, itemFacade);
        this.maxPrice = maxPrice;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {

        if (shoppingBasket == null || shoppingBasket.getStoreId() == null) {
            throw new IllegalArgumentException("ShoppingBasket and StoreId cannot be null");
        }

        double totalBasketPrice = 0.0;
        
        // Calculate total price of entire basket
        for (String productId : shoppingBasket.getOrders().keySet()) {
            double unitPrice = itemFacade.getItem(shoppingBasket.getStoreId(), productId).getPrice();
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