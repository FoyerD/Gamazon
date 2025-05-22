package UI.presenters;

import java.util.List;

import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.Store.FeedbackDTO;

/**
 * Presenter interface for managing store-related operations in the UI layer,
 * following the MVP architectural pattern.
 */
public interface IStorePresenter {

    /**
     * Retrieves store details by its name.
     *
     * @param sessionToken Session identifier for authentication.
     * @param name The name of the store to retrieve.
     * @return Response containing {@link StoreDTO} or an error message.
     */
    Response<StoreDTO> getStoreByName(String sessionToken, String name);

    /**
     * Retrieves all auctions associated with a given store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Unique identifier of the store.
     * @return Response containing a list of {@link AuctionDTO}s or an error.
     */
    Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId);

    /**
     * Retrieves all items available in a specific store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Unique identifier of the store.
     * @return Response containing a list of {@link ItemDTO}s or an error.
     */
    Response<List<ItemDTO>> getItemsByStoreId(String sessionToken, String storeId);

    /**
     * Updates the price of a specific product in a store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId Store ID where the product exists.
     * @param productId ID of the product whose price is to be changed.
     * @param newPrice New price to set.
     * @return Response with true if successful, or false if failed.
     */
    Response<Boolean> updateItemPrice(String sessionToken, String storeId, String productId, float newPrice);

    /**
     * Adds user feedback to a product in a store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store containing the product.
     * @param productId ID of the product to leave feedback for.
     * @param comment The feedback comment.
     * @return Response with true if feedback was added, or false if failed.
     */
    Response<Boolean> addFeedback(String sessionToken, String storeId, String productId, String comment);

    /**
     * Retrieves all feedback for a given store.
     *
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store.
     * @return Response containing a list of {@link FeedbackDTO}s or an error.
     */
    Response<List<FeedbackDTO>> getAllFeedbacksByStoreId(String sessionToken, String storeId);

    /**
     * Adds a new auction for a product in a store.
     * @param sessionToken Session identifier for authentication.
     * @param storeId ID of the store where the auction will be created.
     * @param productId ID of the product to be auctioned.
     * @param auctionEndDate The end date of the auction format.
     * @param startPrice The starting price of the auction.
     * @return
     */
    Response<AuctionDTO> addAuction(String sessionToken, String storeId, String productId, String auctionEndDate, double startPrice);
}
