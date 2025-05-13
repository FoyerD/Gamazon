package UI.presenters;
import Application.ItemService;
import Application.StoreService;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import java.util.List;
import Application.DTOs.AuctionDTO;
import Application.DTOs.ItemDTO;

import org.springframework.stereotype.Component;

@Component
public class StorePresenter implements IStorePresenter {

    private final StoreService storeService;
    private final ItemService itemService;

    public StorePresenter(StoreService storeService) {
        this.storeService = storeService;
    }

    @Override
    public Response<StoreDTO> getStoreByName(String sessionToken, String name) {
        return storeService.getStoreByName(sessionToken, name);
    }

    @Override
    public Response<List<AuctionDTO>> getAllStoreAuctions(String sessionToken, String storeId) {
        return storeService.getAllStoreAuctions(sessionToken, storeId);
    }

    @Override
    public Response<List<ItemDTO>> getItemsByStoreId(String sessionToken, String storeId) {
        return storeService.getItemsByStoreId(sessionToken, storeId);
    }

    
}
