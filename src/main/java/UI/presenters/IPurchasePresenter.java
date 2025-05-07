package UI.presenters;

import java.util.Set;

import Application.DTOs.OrderDTO;

public interface IPurchasePresenter {
    public boolean addProductToCart(String productName, String storeName, int amount);
    public boolean removeProductFromCart(String productName, String storeName);
    public boolean removeProductFromCart(String productName, String storeName, int amount);
    public Set<OrderDTO> viewCart();
    public boolean clearCart();
    public boolean clearBasket(String storeName);
    public boolean makeBid(String productName, String storeName, double bidAmount);
    public boolean purchaseCart(String paymentMethod, String address, String creditCardNumber, String expirationDate, String cvv);
}
