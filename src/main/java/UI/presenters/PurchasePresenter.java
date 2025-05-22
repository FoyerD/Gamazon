package UI.presenters;

import java.util.Date;

import org.springframework.stereotype.Component;

import Application.ShoppingService;
import Application.DTOs.CartDTO;
import Application.utils.Response;

@Component
public class PurchasePresenter implements IPurchasePresenter {
    private final ShoppingService shoppingService;

    public PurchasePresenter(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @Override
    public Response<Boolean> addProductToCart(String sessionToken, String productId, String storeId, int amount) {
        return this.shoppingService.addProductToCart(storeId, sessionToken, productId, amount);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId) {
        return this.shoppingService.removeProductFromCart(storeId, sessionToken, productId);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId, int amount) {
        return this.shoppingService.removeProductFromCart(storeId, sessionToken, productId, amount);
    }

    //This is the real viewCart
    public Response<CartDTO> viewCart(String sessionToken) {
        return this.shoppingService.viewCart(sessionToken);
    }

    @Override
    public Response<Boolean> clearCart(String sessionToken) {
        return this.shoppingService.clearCart(sessionToken);
    }

    @Override
    public Response<Boolean> clearBasket(String sessionToken, String storeId) {
        return this.shoppingService.clearBasket(sessionToken, storeId);
    }


    public Response<Boolean> makeBid(String sessionToken, String auctionId, float bid) {
        throw new UnsupportedOperationException("bid is wrong");
    }

    @Override
    public Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress) {
        return this.shoppingService.makeBid(auctionId, sessionToken, price,
                cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
    }

    @Override
    public Response<Boolean> purchaseCart(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress) {
        return this.shoppingService.checkout(sessionToken, cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
    }
    
}
