package UI.presenters;

import java.util.Set;

import Application.DTOs.OrderDTO;

/**
 * Interface for handling purchase-related user actions such as managing the cart, placing bids, and completing purchases.
 */
public interface IPurchasePresenter {

    /**
     * Adds a specified quantity of a product to the user's cart.
     *
     * @param productName the name of the product
     * @param storeName the name of the store selling the product
     * @param amount the quantity to add
     * @return true if the product was successfully added; false otherwise
     */
    boolean addProductToCart(String sessionToken, String productName, String storeName, int amount);

    /**
     * Completely removes a product from the user's cart.
     *
     * @param productName the name of the product
     * @param storeName the name of the store
     * @return true if the product was successfully removed; false otherwise
     */
    boolean removeProductFromCart(String sessionToken, String productName, String storeName);

    /**
     * Removes a specific amount of a product from the cart.
     *
     * @param productName the name of the product
     * @param storeName the name of the store
     * @param amount the number of items to remove
     * @return true if the specified quantity was removed; false otherwise
     */
    boolean removeProductFromCart(String sessionToken, String productName, String storeName, int amount);

    /**
     * Displays the current contents of the user's cart.
     *
     * @return a set of {@link OrderDTO} representing the cart contents
     */
    Set<OrderDTO> viewCart(String sessionToken);

    /**
     * Empties the entire cart.
     *
     * @return true if the cart was cleared successfully; false otherwise
     */
    boolean clearCart(String sessionToken);

    /**
     * Clears the basket of a specific store within the user's cart.
     *
     * @param storeName the name of the store
     * @return true if the basket was cleared; false otherwise
     */
    boolean clearBasket(String sessionToken, String storeName);

    /**
     * Submits a bid for an auctioned product.
     *
     * @param productName the name of the product
     * @param storeName the store running the auction
     * @param bidAmount the bid value
     * @return true if the bid was successfully placed; false otherwise
     */
    boolean makeBid(String sessionToken, String productName, String storeName, double bidAmount);

    /**
     * Finalizes the purchase of the entire cart using provided payment and shipping details.
     *
     * @param paymentMethod the name of the payment method (e.g., "credit card")
     * @param address the shipping address
     * @param creditCardNumber the credit card number
     * @param expirationDate the card's expiration date in MM/YY format
     * @param cvv the CVV security code
     * @return true if the purchase was successful; false otherwise
     */
    boolean purchaseCart(String sessionToken, String paymentMethod, String address, String creditCardNumber, String expirationDate, String cvv);
}