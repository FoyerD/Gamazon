package UI.presenters;
import Application.CustomerServiceService;
import Application.ItemService;
import Application.StoreService;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.Store.FeedbackDTO;

import java.util.List;
import Application.DTOs.AuctionDTO;
import Application.DTOs.CategoryDTO;
import Application.DTOs.DiscountDTO;
import Application.DTOs.ItemDTO;

import org.springframework.stereotype.Component;

@Component
public class StorePresenter implements IStorePresenter {

    private final StoreService storeService;
    private final ItemService itemService;
    private final CustomerServiceService customerServiceService;

    public StorePresenter(StoreService storeService, ItemService itemService, CustomerServiceService customerServiceService) {
        this.storeService = storeService;
        this.itemService = itemService;
        this.customerServiceService = customerServiceService;
    }

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        return storeService.getStoreByName(sessionToken, name);
    }


    @Override 
    public Response<StoreDTO> getStoreById(String sessionToken, String storeId) {
        return storeService.getStoreById(sessionToken, storeId);
    }

    @Override
    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        return storeService.getAllStoreAuctions(sessionToken, storeId);
    }

    @Override
    public Response<List<ItemDTO>> getItemsByStoreId(String sessionToken, String storeId){
        return itemService.getItemsByStoreId(sessionToken, storeId);
    }

    @Override
    public Response<Boolean> updateItemPrice(String sessionToken, String storeId, String productId, float newPrice) {
        return itemService.changePrice(sessionToken, storeId, productId, newPrice);
    }

    @Override
    public Response<Boolean> addFeedback(String sessionToken, String storeId, String productId, String comment) {
        return customerServiceService.addFeedback(sessionToken, storeId, productId, comment);
    }

    @Override
    public Response<List<FeedbackDTO>> getAllFeedbacksByStoreId(String sessionToken, String storeId) {
        return customerServiceService.getAllFeedbacksByStoreId(sessionToken, storeId);
    }

    @Override
    public Response<AuctionDTO> addAuction(String sessionToken, String storeId, String productId, String auctionEndDate, double startPrice){
        return storeService.addAuction(sessionToken, storeId, productId, auctionEndDate, startPrice);
    }

    @Override
    public Response<ItemDTO> acceptBid(String sessionToken, String storeId, String productId, String auctionId){
        return storeService.acceptBid(sessionToken, storeId, productId, auctionId);
    }

    @Override
    public Response<List<CategoryDTO>> getStoreCategories(String sessionToken, String storeId) {
        return storeService.getAllStoreCategories(sessionToken, storeId);
    }

    @Override
    public Response<DiscountDTO> addDiscount(String sessionToken, String storeId, DiscountDTO discountDTO) {
        return storeService.addDiscount(sessionToken, storeId, discountDTO);
    }

    @Override
    public Response<Boolean> removeDiscount(String sessionToken, String storeId, String discountId) {
        return storeService.removeDiscount(sessionToken, storeId, discountId);
    }

    @Override
    public Response<List<DiscountDTO>> getStoreDiscounts(String sessionToken, String storeId) {
        return storeService.getStoreDiscounts(sessionToken, storeId);
    }
    
}
