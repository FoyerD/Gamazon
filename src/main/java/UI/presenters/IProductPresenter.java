package UI.presenters;

import java.util.List;

import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.utils.Response;
import Domain.Store.ItemFilter;

/**
 * Interface for presenting product-related functionalities to the user.
 * Provides methods to view, filter, and rate both regular and auctioned products.
 */
public interface IProductPresenter {

    /**
     * Retrieves a set of products that match the given filters.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param filters the {@link ItemFilter} object containing the criteria to filter the products
     * @return a {@link Response} containing a list of {@link ItemDTO} that match the specified filters
     */
    Response<List<ItemDTO>> showProductDetails(String sessionToken, ItemFilter filters);

    /**
     * Retrieves product details for a specific product in a specific store.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param filters the filters to apply when retrieving the product details
     * @param storeId the id of the store
     * @return a {@link Response} containing a list of {@link ItemDTO} with the product details from the specified store
     */
    Response<List<ItemDTO>> showProductDetailsOfaStore(String sessionToken, ItemFilter filters, String storeId);

    /**
     * Retrieves all available products across all stores.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @return a {@link Response} containing a set of all {@link ItemDTO} in the marketplace
     */
    Response<List<ItemDTO>> showAllProducts(String sessionToken);

    /**
     * Allows a user to rate and leave feedback for a product in a specific store.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param item the {@link ItemDTO} representing the product to be rated
     * @return a {@link Response} indicating the success or failure of the rating operation
     */
    Response<Void> rateProduct(String sessionToken, ItemDTO item);

    /**
     * Retrieves auction details for a specific product across all stores.
     *
     * @param sessionToken the token representing the current authenticated user session
     * @param filters the filters to apply when retrieving auction details
     * @return a {@link Response} containing a list of {@link AuctionDTO} representing the product in active auctions
     */
    Response<List<AuctionDTO>> showAuctionedProduct(String sessionToken, ItemFilter filters);
}