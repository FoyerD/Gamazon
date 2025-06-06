package UI.presenters;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import Application.ShoppingService;
import Application.DTOs.CartDTO;
import Application.DTOs.ReceiptDTO;
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
    

    @Override
    public Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress) {
        return this.shoppingService.makeBid(auctionId, sessionToken, price,
                cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
    }

    @Override
    public Response<Boolean> purchaseCart(String sessionToken, String userSSN, String cardNumber, Date expiryDate, String cvv,
                           String clientName, String deliveryAddress, String city, String country, String zipCode) {
        return this.shoppingService.checkout(sessionToken, userSSN, cardNumber, expiryDate, cvv, clientName, deliveryAddress, 
                                             city, country, zipCode);
    }

    @Override
    public Response<List<ReceiptDTO>> getPersonalPurcahses(String sessionToken) {
        return this.shoppingService.getUserPurchaseHistory(sessionToken);
    }
    
}
