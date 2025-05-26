package Domain.Store.Discounts;

import java.util.Map;
import java.util.Set;

import Domain.Shopping.ShoppingBasket;
import Domain.Store.ItemFacade;

public class OrDiscount extends CompositeDiscount {

    public OrDiscount(ItemFacade itemFacade, Discount discount1, Discount discount2) {
        super(itemFacade, Set.of(discount1, discount2));
    }

    public OrDiscount(ItemFacade itemFacade, Set<Discount> discounts) {
        super(itemFacade, discounts);
    }

    @Override
    public Map<String, PriceBreakDown> calculatePrice(ShoppingBasket basket) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculatePrice'");
    }

    @Override
    public boolean isQualified(String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isQualified'");
    }

}
