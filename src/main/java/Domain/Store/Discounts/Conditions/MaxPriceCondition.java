package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MaxPriceCondition extends SimpleCondition{

    private double maxPrice;
    private String productId;
    private String storeId;

    public MaxPriceCondition(ItemFacade itemFacade, String storeId, String productId, double maxPrice) {
        super(itemFacade);
        this.storeId = storeId;
        this.productId = productId;
        this.maxPrice = maxPrice;
    }

    // Constructor for loading from repository with existing UUID
    public MaxPriceCondition(UUID id, ItemFacade itemFacade, String storeId, String productId, double maxPrice) {
        super(id, itemFacade);
        this.storeId = storeId;
        this.productId = productId;
        this.maxPrice = maxPrice;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        double unitPrice = itemFacade.getItem(storeId, productId).getPrice();
        double price = unitPrice * shoppingBasket.getQuantity(productId);
        return price <= maxPrice;
    }

    // Getters for repository serialization
    public double getMaxPrice() {
        return maxPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreId() {
        return storeId;
    }
}