package UI.presenters;

import java.util.Set;

import Application.DTOs.ItemDTO;

/**
 * Interface for presenting product-related functionalities to the user.
 * Provides methods to view, filter, and rate both regular and auctioned products.
 */
public interface IProductPresenter {

    /**
     * Retrieves a set of products that match the given product name.
     *
     * @param productName the name of the product to search for
     * @return a set of {@link ItemDTO} matching the product name
     */
    Set<ItemDTO> showProductDetails(String productName);

    /**
     * Retrieves product details for a specific product in a specific store.
     *
     * @param productName the name of the product
     * @param storeName the name of the store
     * @return an {@link ItemDTO} with the product details from the given store
     */
    ItemDTO showProductDetailsOfaStore(String productName, String storeName);

    /**
     * Retrieves all available products across all stores.
     *
     * @return a set of all {@link ItemDTO} in the marketplace
     */
    Set<ItemDTO> showAllProducts();

    /**
     * Retrieves products that belong to the specified categories.
     *
     * @param categories a set of category names to filter products
     * @return a set of {@link ItemDTO} matching the specified categories
     */
    Set<ItemDTO> showProductsByCategories(Set<String> categories);

    /**
     * Allows a user to rate and leave feedback for a product in a specific store.
     *
     * @param productName the name of the product
     * @param storeName the name of the store offering the product
     * @param rating a numeric rating (e.g., from 1.0 to 5.0)
     * @param feedback user-provided feedback text
     */
    void rateProduct(String productName, String storeName, double rating, String feedback);

    /**
     * Retrieves all products that are currently under auction across all stores.
     *
     * @return a set of auctioned {@link ItemDTO}
     */
    Set<ItemDTO> showAuctionedProducts();

    /**
     * Retrieves auctioned products filtered by categories.
     *
     * @param categories a set of category names to filter auctioned products
     * @return a set of auctioned {@link ItemDTO} in the specified categories
     */
    Set<ItemDTO> showAuctionedProductsByCategories(Set<String> categories);

    /**
     * Retrieves auction details for a specific product across all stores.
     *
     * @param productName the name of the auctioned product
     * @return a set of {@link ItemDTO} representing the product in active auctions
     */
    Set<ItemDTO> showAuctionedProduct(String productName);
}