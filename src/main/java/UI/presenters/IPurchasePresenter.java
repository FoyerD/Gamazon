package UI.presenters;

import java.util.Set;
import Application.DTOs.OrderDTO;
import Application.utils.Response;

/**
 * Interface for handling purchase-related user actions such as managing the cart, placing bids, and completing purchases.
 */
public interface IPurchasePresenter {

    /**
     * Adds a specified quantity of a product to the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productName the name of the product
     * @param storeName the name of the store selling the product
     * @param amount the quantity to add
     * @return a {@link Response} indicating whether the product was successfully added
     */
    Response<Boolean> addProductToCart(String sessionToken, String productName, String storeName, int amount);

    /**
     * Completely removes a product from the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productName the name of the product
     * @param storeName the name of the store
     * @return a {@link Response} indicating whether the product was successfully removed
     */
    Response<Boolean> removeProductFromCart(String sessionToken, String productName, String storeName);

    /**
     * Removes a specific amount of a product from the cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productName the name of the product
     * @param storeName the name of the store
     * @param amount the number of items to remove
     * @return a {@link Response} indicating whether the specified quantity was removed
     */
    Response<Boolean> removeProductFromCart(String sessionToken, String productName, String storeName, int amount);

    /**
     * Displays the current contents of the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @return a {@link Response} containing a set of {@link OrderDTO} representing the cart contents
     */
    Response<Set<OrderDTO>> viewCart(String sessionToken);

    /**
     * Empties the entire cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @return a {@link Response} indicating whether the cart was cleared successfully
     */
    Response<Boolean> clearCart(String sessionToken);

    /**
     * Clears the basket of a specific store within the user's cart.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param storeName the name of the store
     * @return a {@link Response} indicating whether the basket was cleared successfully
     */
    Response<Boolean> clearBasket(String sessionToken, String storeName);

    /**
     * Submits a bid for an auctioned product.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param productName the name of the product
     * @param storeName the store running the auction
     * @param bidAmount the bid value
     * @return a {@link Response} indicating whether the bid was successfully placed
     */
    Response<Boolean> makeBid(String sessionToken, String productName, String storeName, double bidAmount);

    /**
     * Finalizes the purchase of the entire cart using provided payment and shipping details.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param paymentMethod the name of the payment method (e.g., "credit card")
     * @param address the shipping address
     * @param creditCardNumber the credit card number
     * @param expirationDate the card's expiration date in MM/YY format
     * @param cvv the CVV security code
     * @return a {@link Response} indicating whether the purchase was successful
     */
    Response<Boolean> purchaseCart(String sessionToken, String paymentMethod, String address, String creditCardNumber, String expirationDate, String cvv);
}