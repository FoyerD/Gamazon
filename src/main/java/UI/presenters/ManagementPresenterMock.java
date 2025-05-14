package UI.presenters;

import java.util.List;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType;

public class ManagementPresenterMock implements IManagementPresenter {
      @Override
    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        StoreDTO mockStore = new StoreDTO("MOCK-STORE-1", name, description, "MOCK-FOUNDER", true, null, null);
        return new Response<>(mockStore);
    }

    @Override
    public Response<Boolean> openStore(String sessionToken, String storeId) {
        return new Response<>(true);
    }

    @Override
    public Response<Boolean> closeStore(String sessionToken, String storeId) {
        return new Response<>(true);
    }    @Override
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, String description) {
        ItemDTO mockItem = new ItemDTO(storeId, productId, 0.0, 0, description, null, "Mock Item", 0.0);
        return new Response<>(mockItem);
    }

    @Override
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, double price, int amount, String description) {
        ItemDTO mockItem = new ItemDTO(storeId, productId, price, amount, description, null, "Mock Item", 0.0);
        return new Response<>(mockItem);
    }

    @Override
    public Response<ItemDTO> removeItem(String sessionToken, String storeId, String productId) {
        ItemDTO mockItem = new ItemDTO(storeId, productId, 0.0, 0, "Removed item", null, "Mock Item", 0.0);
        return new Response<>(mockItem);
    }

    @Override
    public Response<Void> increaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return new Response<>(null);
    }

    @Override
    public Response<Void> decreaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return new Response<>(null);
    }

    @Override
    public Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        return new Response<>(null);
    }

    @Override
    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername, String storeId) {
        return new Response<>(null);
    }

    @Override
    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        return new Response<>(null);
    }

    @Override
    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        return new Response<>(null);
    }
}
