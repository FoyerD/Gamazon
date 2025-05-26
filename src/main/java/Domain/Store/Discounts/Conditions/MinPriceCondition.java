package Domain.Store.Discounts.Conditions;

import java.util.UUID;
import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class MinPriceCondition extends SimpleCondition {

    private double minPrice;
    private String productId;
    private String storeId;

    public MinPriceCondition(ItemFacade itemFacade, String storeId, String productId, double minPrice) {
        super(itemFacade);
        this.productId = productId;
        this.minPrice = minPrice;
        this.storeId = storeId;
    }

    // Constructor for loading from repository with existing UUID
    public MinPriceCondition(UUID id, ItemFacade itemFacade, String storeId, String productId, double minPrice) {
        super(id, itemFacade);
        this.productId = productId;
        this.minPrice = minPrice;
        this.storeId = storeId;
    }

    @Override
    public boolean isSatisfied(ShoppingBasket shoppingBasket) {
        double unitPrice = itemFacade.getItem(storeId, productId).getPrice();
        double price = unitPrice * shoppingBasket.getQuantity(productId);
        return price >= minPrice;
    }

    // Getters for repository serialization
    public double getMinPrice() {
        return minPrice;
    }

    public String getProductId() {
        return productId;
    }

    public String getStoreId() {
        return storeId;
    }
}