package UI.presenters;

import java.util.Date;
import Application.DTOs.CartDTO;
import Application.utils.Response;

/**
 * Interface for handling purchase-related user actions such as managing the cart, placing bids, and completing purchases.
 */
public interface IPurchasePresenter {

    /**
     * Adds a specified quantity of a product to the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productId the unique identifier of the product
     * @param storeId the unique identifier of the store selling the product
     * @param amount the quantity of the product to add
     * @return a {@link Response} indicating whether the product was successfully added to the cart
     */
    Response<Boolean> addProductToCart(String sessionToken, String productId, String storeId, int amount);

    /**
     * Completely removes a product from the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productId the unique identifier of the product
     * @param storeId the unique identifier of the store selling the product
     * @return a {@link Response} indicating whether the product was successfully removed from the cart
     */
    Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId);

    /**
     * Removes a specific quantity of a product from the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productId the unique identifier of the product
     * @param storeId the unique identifier of the store selling the product
     * @param amount the quantity of the product to remove
     * @return a {@link Response} indicating whether the specified quantity was successfully removed from the cart
     */
    Response<Boolean> removeProductFromCart(String sessionToken, String productId, String storeId, int amount);

    /**
     * Displays the current contents of the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @return a {@link Response} containing a {@link CartDTO} representing the items in the cart
     */
    Response<CartDTO> viewCart(String sessionToken);

    /**
     * Empties the entire cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @return a {@link Response} indicating whether the cart was successfully cleared
     */
    Response<Boolean> clearCart(String sessionToken);

    /**
     * Clears the basket of a specific store within the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param storeId the unique identifier of the store whose basket is to be cleared
     * @return a {@link Response} indicating whether the basket was successfully cleared
     */
    Response<Boolean> clearBasket(String sessionToken, String storeId);

    /**
     * Submits a bid for an auctioned product.
     *
     * @param auctionId the unique identifier of the auction
     * @param sessionToken the token representing the current authenticated user session
     * @param price the value of the bid being placed
     * @param cardNumber the credit card number used for payment
     * @param expiryDate the expiration date of the credit card
     * @param cvv the CVV security code of the credit card
     * @param andIncrement an increment value for tracking or processing purposes
     * @param clientName the name of the client placing the bid
     * @param deliveryAddress the address where the product will be delivered if the bid is successful
     * @return a {@link Response} indicating whether the bid was successfully placed
     */
    Response<Boolean> makeBid(String auctionId, String sessionToken, float price,
                                    String cardNumber, Date expiryDate, String cvv,
                                    long andIncrement, String clientName, String deliveryAddress);

    // TODO: Remove this method
    // /**
    //  * Submits a bid for an auctioned product.
    //  *
    //  * @param sessionToken the token representing the current authenticated user session
    //  * @param auctionId the unique identifier of the auction
    //  * @param bid the value of the bid being placed
    //  * @return a {@link Response} indicating whether the bid was successfully placed
    //  */
    // Response<Boolean> makeBid(String sessionToken, String auctionId, float bid);

    /**
     * Finalizes the purchase of the entire cart using the provided payment and shipping details.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param cardNumber the credit card number used for payment
     * @param expiryDate the expiration date of the credit card
     * @param cvv the CVV security code of the credit card
     * @param andIncrement an increment value for tracking or processing purposes
     * @param clientName the name of the client making the purchase
     * @param deliveryAddress the address where the purchased items will be delivered
     * @return a {@link Response} indicating whether the purchase was successfully completed
     */
    Response<Boolean> purchaseCart(String sessionToken, String cardNumber, Date expiryDate, String cvv, long andIncrement,
         String clientName, String deliveryAddress);
    



}