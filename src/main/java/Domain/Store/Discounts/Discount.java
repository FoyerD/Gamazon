package Domain.Store.Discounts;

import java.util.Map;
import Domain.Store.Discounts.Conditions.Condition;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;
import Domain.Store.Discounts.Conditions.TrueCondition;

public abstract class Discount {

    protected ItemFacade itemFacade;

    protected Condition condition;


    public Discount(ItemFacade itemFacade, Condition condition) {
        this.itemFacade = itemFacade;
        this.condition = condition;
    }

    public Discount(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
        this.condition = new TrueCondition();
    }



    // Outputs a map from product to the price breakdown
    public abstract Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket);


    public abstract boolean isQualified(String productId);

    public boolean conditionApplies(ShoppingBasket basket) {
        if (condition == null) {
            return true; // If no condition is set, we assume it applies
        }
        return condition.isSatisfied(basket);
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

}
