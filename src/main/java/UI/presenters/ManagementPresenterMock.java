package UI.presenters;

import Application.DTOs.ItemDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ManagementPresenterMock implements IManagementPresenter {
    private final Map<String, List<String>> storeManagers = new ConcurrentHashMap<>();
    private final Map<String, List<String>> storeOwners = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<PermissionType>>> managerPermissions = new ConcurrentHashMap<>();

    @Override
    public Response<StoreDTO> addStore(String sessionToken, String name, String description) {
        StoreDTO store = new StoreDTO(
            UUID.randomUUID().toString(),
            name,
            description,
            sessionToken, // using sessionToken as founder for mock
            true,
            new HashSet<>(),
            new HashSet<>()
        );
        return Response.success(store);
    }

    @Override
    public Response<Boolean> openStore(String sessionToken, String storeId) {
        return Response.success(true);
    }

    @Override
    public Response<Boolean> closeStore(String sessionToken, String storeId) {
        return Response.success(true);
    }

    @Override
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, String description) {
        ItemDTO item = new ItemDTO(
            storeId,
            productId,
            0.0,
            0,
            description,
            new HashSet<>(),
            "New Item",
            0.0
        );
        return Response.success(item);
    }

    @Override
    public Response<ItemDTO> addItem(String sessionToken, String storeId, String productId, double price, int amount, String description) {
        ItemDTO item = new ItemDTO(
            storeId,
            productId,
            price,
            amount,
            description,
            new HashSet<>(),
            "New Item",
            0.0
        );
        return Response.success(item);
    }

    @Override
    public Response<ItemDTO> removeItem(String sessionToken, String storeId, String productId) {
        ItemDTO item = new ItemDTO(
            storeId,
            productId,
            0.0,
            0,
            "Item removed",
            new HashSet<>(),
            "Removed Item",
            0.0
        );
        return Response.success(item);
    }

    @Override
    public Response<Void> increaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return Response.success(null);
    }

    @Override
    public Response<Void> decreaseItemAmount(String sessionToken, String storeId, String productId, int amount) {
        return Response.success(null);
    }

    @Override
    public Response<Void> appointStoreManager(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        storeManagers.computeIfAbsent(storeId, k -> new ArrayList<>()).add(appointeeUsername);
        // Initialize with basic permissions
        managerPermissions
            .computeIfAbsent(storeId, k -> new HashMap<>())
            .put(appointeeUsername, Arrays.asList(
                PermissionType.ADMINISTER_STORE,
                PermissionType.OVERSEE_OFFERS
            ));
        return Response.success(null);
    }

    @Override
    public Response<Void> removeStoreManager(String sessionToken, String removerUsername, String managerUsername, String storeId) {
        if (storeManagers.containsKey(storeId)) {
            storeManagers.get(storeId).remove(managerUsername);
            if (managerPermissions.containsKey(storeId)) {
                managerPermissions.get(storeId).remove(managerUsername);
            }
        }
        return Response.success(null);
    }

    @Override
    public Response<Void> appointStoreOwner(String sessionToken, String appointerUsername, String appointeeUsername, String storeId) {
        storeOwners.computeIfAbsent(storeId, k -> new ArrayList<>()).add(appointeeUsername);
        return Response.success(null);
    }

    @Override
    public Response<Void> changeManagerPermissions(String sessionToken, String ownerUsername, String managerUsername, String storeId, List<PermissionType> newPermissions) {
        if (managerPermissions.containsKey(storeId)) {
            managerPermissions.get(storeId).put(managerUsername, newPermissions);
        }
        return Response.success(null);
    }

    // Helper methods to get mock data
    public List<String> getStoreManagers(String storeId) {
        return storeManagers.getOrDefault(storeId, new ArrayList<>());
    }

    public List<String> getStoreOwners(String storeId) {
        return storeOwners.getOrDefault(storeId, new ArrayList<>());
    }

    public List<PermissionType> getManagerPermissions(String storeId, String managerUsername) {
        return managerPermissions
            .getOrDefault(storeId, new HashMap<>())
            .getOrDefault(managerUsername, new ArrayList<>());
    }
} 