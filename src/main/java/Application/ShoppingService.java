public class ShoppingService implements IShoppingService {
    private final IShoppingCartFacade;
    private final IShoppingBasketFacade;

    
    public ShoppingService(IShoppingBasketRepository basketRepository, IShoppingCartRepository cartRepository) {
        cartFacade = new ShoppingCartFacade(cartRepository);
        basketFacade = new ShoppingBasketFacade(basketRepository);
    }

    @Override
    public void addProductToCart(PurchaseInfo info) {
        if (!basketFacade.hasSufficientInventory(info.getProductId(), info.getQuantity())) {
            throw new RuntimeException("Product out of stock");
        }
        cartFacade.addProductToCart(info);
    }

    @Override
    public IShoppingCart getCart(String clientId) {
        return cartFacade.getCart(clientId);
    }

    @Override
    public IShoppingBasket getBasket(String clientId, String storeId) {
        return cartFacade.getBasket(clientId, storeId);
    }

    @Override
    public void immediatePurchase(PurchaseInfo info) {
        IShoppingBasket basket = getBasket(info.getClientId(), info.getStoreId());
        if (basket == null || basket.getItems().getOrDefault(info.getProductId(), 0) < info.getQuantity()) {
            throw new RuntimeException("Item not in cart or insufficient quantity");
        }

        if (!basketFacade.hasSufficientInventory(info.getProductId(), info.getQuantity())) {
            throw new RuntimeException("Insufficient inventory");
        }

        if (info.getPaymentDetails() == null || info.getPaymentDetails().isBlank()) {
            throw new RuntimeException("Invalid payment details");
        }

        basketFacade.decreaseInventory(info.getProductId(), info.getQuantity());
        basket.removeItem(info.getProductId());

        System.out.println("Purchase complete for client " + info.getClientId() + 
                           " of " + info.getQuantity() + " units of product " + info.getProductId());
    }

    @Override
    public void makeBid(PurchaseInfo info) {
        System.out.println("Bid submitted: " + info.getBidPrice() + " for product " + info.getProductId());
    }

    @Override
    public void joinAuction(PurchaseInfo info) {
        System.out.println("Joined auction: " + info.getBidPrice() + " for product " + info.getProductId());
    }

    @Override
    public void joinLottery(PurchaseInfo info) {
        System.out.println("Joined lottery for product " + info.getProductId());
    }

    public void addInventory(String productId, int quantity) {
        basketFacade.addInventory(productId, quantity);
    }
}
