package Application;
import java.util.Map;

import java.util.Date;

import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.ItemFacade;
import Domain.ExternalServices.IPaymentService;
import Domain.Store.StoreFacade;



public class ShoppingService{
    private final IShoppingCartFacade cartFacade;

    public ShoppingService(IShoppingCartRepository cartRepository, IShoppingBasketRepository basketRepository, ItemFacade itemFacade, IReceiptRepository receiptRepo, StoreFacade storeFacade) { 
        cartFacade = new ShoppingCartFacade(cartRepository, basketRepository, receiptRepo, new MockPaymentService(), itemFacade, storeFacade);

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

    public Response<Map<String,Map<String, Integer>>> viewCart(String clientId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            Map<String,Map<String, Integer>> itemsMapPerStore = cartFacade.viewCart(clientId);
            return new Response<>(itemsMapPerStore);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    public Response<Boolean> removeProductFromCart(String storeId, String clientId, String productId, int quantity) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.removeProductFromCart(storeId, clientId, productId, quantity);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> removeProductFromCart(String storeId, String clientId, String productId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.removeProductFromCart(storeId, clientId, productId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearCart(String clientId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.clearCart(clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearBasket(String clientId, String storeId) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.clearBasket(clientId, storeId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    // processPayment(String card_owner, String card_number, Date expiry_date, String cvv, double price,
    //         long andIncrement, String name, String deliveryAddress);

    // Make Immidiate Purchase Use Case 2.5
    public Response<Boolean> checkout(String cardOwnerID, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));


            //  public boolean checkout(String clientId, String card_number, Date expiry_date, String cvv) {

            cartFacade.checkout(cardOwnerID, cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    
    public Response<Boolean> makeBid(String auctionId, String clientId, float price) {
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.makeBid(auctionId, clientId, price);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    class MockPaymentService implements IPaymentService {
        @Override
        public Response<Boolean> processPayment(String card_owner, String card_number, Date expiry_date, String cvv,
            double price, long andIncrement, String name, String deliveryAddress) {
            // Mock payment processing logic
            return new Response<>(true); // Assume payment is always successful for testing
        }

        @Override
        public void updatePaymentServiceURL(String url) {
            // Mock implementation for updating payment service URL
            System.out.println("Payment service URL updated to: " + url);
        }

        @Override
        public void initialize() {
            // Mock initialization logic
            System.out.println("MockPaymentService initialized.");
        }

    }

}
