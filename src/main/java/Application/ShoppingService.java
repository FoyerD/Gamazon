package Application;
import java.util.Date;
import java.util.Set;

import Application.DTOs.CartDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.ShoppingBasketDTO;
import Application.utils.Error;
import Application.utils.Response;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Domain.ExternalServices.IPaymentService;
import Domain.Pair;
import Domain.Shopping.IReceiptRepository;
import Domain.Shopping.IShoppingBasketRepository;
import Domain.Shopping.IShoppingCartFacade;
import Domain.Shopping.IShoppingCartRepository;
import Domain.Shopping.ShoppingCartFacade;
import Domain.Store.IProductRepository;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.StoreFacade;

@Service
public class ShoppingService{
    private final IShoppingCartFacade cartFacade;
    private final TokenService tokenService;

    public ShoppingService(IShoppingCartRepository cartRepository, IShoppingBasketRepository basketRepository,
     ItemFacade itemFacade, StoreFacade storeFacade, IReceiptRepository receiptRepository,
      IProductRepository productRepository, TokenService tokenService) {
        this.tokenService = tokenService;
        cartFacade = new ShoppingCartFacade(cartRepository, basketRepository, new MockPaymentService(), itemFacade, storeFacade, receiptRepository, productRepository);
    }

    @Autowired
    public ShoppingService(IShoppingCartFacade cartFacade, TokenService tokenService, StoreFacade storeFacade) {
        this.cartFacade = cartFacade;
        this.tokenService = tokenService;
    }

    public Response<Boolean> addProductToCart(String storeId, String sessionToken, String productId, int quantity) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);

        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.addProductToCart(storeId, clientId, productId, quantity);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<CartDTO> viewCart(String sessionToken) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));
            Set<Pair<Item, Integer>> itemsMap = cartFacade.viewCart(clientId);
            Map<String, ShoppingBasketDTO> baskets = new HashMap<>();
            for (Pair<Item, Integer> item : itemsMap) {
                ItemDTO itemDTO = ItemDTO.fromItem(item.getFirst());
                itemDTO.setAmount(item.getSecond());
                
                if(baskets.containsKey(item.getFirst().getStoreId())){
                    baskets.get(item.getFirst().getStoreId()).getOrders().put(item.getFirst().getProductId(), itemDTO);
                } else {
                    ShoppingBasketDTO basket = new ShoppingBasketDTO(item.getFirst().getStoreId(), clientId, new HashMap<>());
                    basket.getOrders().put(item.getFirst().getProductId(), itemDTO);
                    baskets.put(item.getFirst().getStoreId(), basket);
                }
            }
            CartDTO cart = new CartDTO(clientId, baskets);
            return new Response<>(cart);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    public Response<Boolean> removeProductFromCart(String storeId, String sessionToken, String productId, int quantity) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.removeProductFromCart(storeId, clientId, productId, quantity);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> removeProductFromCart(String storeId, String sessionToken, String productId) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.removeProductFromCart(storeId, clientId, productId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearCart(String sessionToken) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.clearCart(clientId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> clearBasket(String sessionToken, String storeId) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));

            cartFacade.clearBasket(clientId, storeId);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    // Make Immidiate Purchase Use Case 2.5
    public Response<Boolean> checkout(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }
        String clientId = this.tokenService.extractId(sessionToken);
        
        try {
            if(this.cartFacade == null) return new Response<>(new Error("cartFacade is not initialized."));


            //  public boolean checkout(String clientId, String card_number, Date expiry_date, String cvv) {

            cartFacade.checkout(clientId, cardNumber, expiryDate, cvv, andIncrement, clientName, deliveryAddress);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    
    public Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress) {
        if (!tokenService.validateToken(sessionToken)) {
            return Response.error("Invalid token");
        }

        String clientId = this.tokenService.extractId(sessionToken);

        try {
            if (this.cartFacade == null) {
                return new Response<>(new Error("cartFacade is not initialized."));
            }

            cartFacade.makeBid(auctionId, clientId, price,
                            cardNumber, expiryDate, cvv,
                            andIncrement, clientName, deliveryAddress);

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
