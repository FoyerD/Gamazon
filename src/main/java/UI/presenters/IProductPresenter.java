package UI.presenters;

import java.util.Set;

import Application.DTOs.ItemDTO;

public interface IProductPresenter {
    public Set<ItemDTO> showProductDetails(String productName);
    public ItemDTO showProductDetailsOfaStore(String productName, String storeName);
    public Set<ItemDTO> showAllProducts();
    public Set<ItemDTO> showProductsByCategories(Set<String> categories);
    public void rateProduct(String productName, String storeName, double rating, String feedback);
    public Set<ItemDTO> showAuctionedProducts();
    public Set<ItemDTO> showAuctionedProductsByCategories(Set<String> categories);
    public Set<ItemDTO> showAuctionedProduct(String productName);  
} 