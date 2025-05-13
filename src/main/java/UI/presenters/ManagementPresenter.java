package UI.presenters;

import java.util.List;

import Application.ItemService;
import Application.MarketService;
import Application.StoreService;
import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType;

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
        return marketService.addStore(sessionToken, name, description);
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
    public Response<Boolean> addItem(String sessionToken, ItemDTO item) {
        return itemService.add(sessionToken, null, null); // to fix
    }

    @Override
    public Response<ItemDTO> removeItem(String sessionToken, String storeId, String productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItem'");
    }

    @Override
    public Response<Void> increaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'increaseItemAmount'");
    }

    @Override
    public Response<Void> decreaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'decreaseItemAmount'");
    }

    @Override
    public Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername,
            String storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appointStoreManager'");
    }

    @Override
    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername,
            String storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeStoreManager'");
    }

    @Override
    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername,
            String storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'appointStoreOwner'");
    }

    @Override
    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername,
            String storeId, List<PermissionType> newPermissions) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changeManagerPermissions'");
    }
    
}
 