package Domain.Shopping;

public class ShoppingCart implements IShoppingCart {

    private String clientId;
    private Set<IShoppingBasket> baskets;

    public ShoppingCart(String clientId) {
        this.clientId = clientId;
        this.baskets = new HashSet<>();
    }


    public ShoppingCart(String clientId, Set<IShoppingBasket> baskets) {
        this.clientId = clientId;
        this.baskets = baskets;
    }

    public String getClientId() {
        return clientId;
    }


    public void Checkout() {
        throw new UnsupportedOperationException("Not implemented yet!!");
    }

    
}
