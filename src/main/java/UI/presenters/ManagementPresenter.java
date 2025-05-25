package UI.presenters;

import java.util.List;

import org.springframework.stereotype.Component;

import Application.ItemService;
import Application.MarketService;
import Application.StoreService;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType;
import Domain.Pair;

@Component
public class ManagementPresenter implements IManagementPresenter {

    private final MarketService marketService;
    private final StoreService storeService;
    private final ItemService itemService;

    public ManagementPresenter(MarketService marketService, StoreService storeService, ItemService itemService){
        this.marketService = marketService;
        this.storeService = storeService;
        this.itemService = itemService;
    }

    @Override
    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        return storeService.addStore(sessionToken, name, description);
    }

    @Override
    public Response<Boolean> openStore(String sessionToken, String storeId) {
        return storeService.openStore(sessionToken, storeId);
    }

    @Override
    public Response<Boolean> closeStore(String sessionToken, String storeId) {
        return storeService.closeStore(sessionToken, storeId);
    }

    @Override
    public Response<Boolean> closeStoreNotPermanent(String sessionToken, String storeId) {
        return storeService.closeStoreNotPermanent(sessionToken, storeId);
    }

    @Override
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, String description) {
        return itemService.add(sessionToken, storeId, productId, description);
    }

    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, double price, int amount, String description){
        return itemService.add(sessionToken, storeId, productId, price, amount, description);
    }

    @Override
    public Response<ItemDTO> removeItem(String sessionToken, String storeId, String productId) {
        return itemService.remove(sessionToken,new Pair<>(storeId, productId));
    }

    @Override
    public Response<Void> increaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return itemService.increaseAmount(sessionToken, new Pair<>(storeId, productId), amount);
    }

    @Override
    public Response<Void> decreaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return itemService.decreaseAmount(sessionToken, new Pair<>(storeId, productId), amount);
    }

    @Override
    public Response<Void> appointStoreManager(String sessionToken, String appointeeId,
            String storeId) {
            return marketService.appointStoreManager(sessionToken, appointeeId, storeId);
    }

    @Override
    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername,
            String storeId) {
        return marketService.removeStoreManager(sessionToken, removerUsername, managerUsername, storeId);
    }

    @Override
    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername,
            String storeId) {
        return marketService.appointStoreOwner(sessionToken, appointerUsername, appointeeUsername, storeId);
    }

    @Override
    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername,
            String storeId, List<PermissionType> newPermissions) {
        return marketService.changeManagerPermissions(sessionToken, ownerUsername, managerUsername, storeId, newPermissions);
    }
    
}
 