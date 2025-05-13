package UI.presenters;

import java.util.Date;
import java.util.Set;

import org.springframework.stereotype.Component;

import Application.ShoppingService;
import Application.DTOs.OrderDTO;
import Application.utils.Response;

@Component
public class PurchasePresenter implements IPurchasePresenter {
    private final ShoppingService shoppingService;

    public PurchasePresenter(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @Override
    public Response<Boolean> addProductToCart(String sessionToken, String productId, String storeId, int amount) {
        return this.shoppingService.addProductToCart(sessionToken, productId, storeId, amount);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId) {
        return this.shoppingService.removeProductFromCart(sessionToken, productId, storeId);
    }

    @Override
    public Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId, int amount) {
        return this.shoppingService.removeProductFromCart(sessionToken, productId, storeId, amount);
    }

    @Override
    public Response<Set<OrderDTO>> viewCart(String sessionToken) {
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

    @Override
    public Response<Boolean> makeBid(String sessionToken, String auctionId, float bid) {
        return this.shoppingService.makeBid(sessionToken, auctionId, bid);
    }

    @Override
    public Response<Boolean> purchaseCart(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress) {
        return this.shoppingService.checkout(sessionToken, cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
    }
    
}
