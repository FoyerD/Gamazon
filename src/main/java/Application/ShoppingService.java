package Application;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCart;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Shopping.ShoppingCartFacade;

public class ShoppingService{
    private final IShoppingCartFacade cartFacade;

    
    public ShoppingService(IShoppingCartRepository cartRepository, IShoppingBasketRepository basketRepository) {
        cartFacade = new ShoppingCartFacade(cartRepository, basketRepository);
    }

    public Response<Boolean> addProductToCart(String storeId, String clientId, String productId, int quantity) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.addProductToCart(storeId, clientId, productId, quantity);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<IShoppingCart> getCart(String clientId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            IShoppingCart cart = cartFacade.getCart(clientId);
            return new Response<>(cart);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ShoppingBasket> getBasket(String clientId, String storeId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            ShoppingBasket basket = cartFacade.getBasket(clientId, storeId);
            return new Response<>(basket);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    // public Response<Boolean> immediatePurchase(PurchaseInfo info) {
    //     IShoppingBasket basket = getBasket(info.getClientId(), info.getStoreId());
    //     if (basket == null || basket.getItems().getOrDefault(info.getProductId(), 0) < info.getQuantity()) {
    //         throw new RuntimeException("Item not in cart or insufficient quantity");
    //     }

    //     if (!basketFacade.hasSufficientInventory(info.getProductId(), info.getQuantity())) {
    //         throw new RuntimeException("Insufficient inventory");
    //     }

    //     if (info.getPaymentDetails() == null || info.getPaymentDetails().isBlank()) {
    //         throw new RuntimeException("Invalid payment details");
    //     }

    //     basketFacade.decreaseInventory(info.getProductId(), info.getQuantity());
    //     basket.removeItem(info.getProductId(), info.getQuantity());

    //     System.out.println("Purchase complete for client " + info.getClientId() + 
    //                        " of " + info.getQuantity() + " units of product " + info.getProductId());
    // }

    // public Response<Boolean> makeBid(PurchaseInfo info) {
    //     System.out.println("Bid submitted: " + info.getBidPrice() + " for product " + info.getProductId());
    // }

    // public Response<Boolean> joinAuction(PurchaseInfo info) {
    //     System.out.println("Joined auction: " + info.getBidPrice() + " for product " + info.getProductId());
    // }

    // public Response<Boolean> joinLottery(PurchaseInfo info) {
    //     System.out.println("Joined lottery for product " + info.getProductId());
    // }

}
