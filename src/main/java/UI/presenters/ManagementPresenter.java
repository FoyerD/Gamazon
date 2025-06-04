package UI.presenters;

import java.util.List;

import org.springframework.stereotype.Component;

import Application.ItemService;
import Application.MarketService;
import Application.PolicyService;
import Application.StoreService;
import Application.DTOs.ClientOrderDTO;
import Application.DTOs.EmployeeInfo;
import Application.DTOs.ItemDTO;
import Application.DTOs.PolicyDTO;
import Application.DTOs.StoreDTO;
import Application.utils.Response;
import Domain.management.PermissionType;
import Domain.Pair;

@Component
public class ManagementPresenter implements IManagementPresenter {

    private final MarketService marketService;
    private final StoreService storeService;
    private final ItemService itemService;
    private final PolicyService policyService;

    public ManagementPresenter(MarketService marketService, StoreService storeService, ItemService itemService, PolicyService policyService){
        this.marketService = marketService;
        this.storeService = storeService;
        this.itemService = itemService;
        this.policyService = policyService;
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
    public Response<Void> removeStoreOwner(String sessionToken,  String managerId,
            String storeId) {
        return marketService.removeStoreOwner(sessionToken, managerId, storeId);
    }

    @Override
    public Response<Void> appointStoreOwner(String sessionToken, String appointeeId,
            String storeId) {
        return marketService.appointStoreOwner(sessionToken, appointeeId, storeId);
    }

    @Override
    public Response<Void> changeManagerPermissions(String sessionToken, String managerId,
            String storeId, List<PermissionType> newPermissions) {
        return marketService.changeManagerPermissions(sessionToken, managerId, storeId, newPermissions);
    }

    @Override
    public Response<EmployeeInfo> getEmployeeInfo(String sessionToken, String storeId) {
        return marketService.getEmployeeInfo(sessionToken, storeId);
    }


    @Override
    public Response<List<ClientOrderDTO>> getPurchaseHistory(String sessionToken, String storeId) {
        return marketService.getStorePurchaseHistory(sessionToken, storeId);
    }

    @Override
    public Response<PolicyDTO> savePolicy(String sessionToken, PolicyDTO policy) {
        return policyService.createPolicy(sessionToken, policy.getStoreId(), policy);
    }

    @Override
    public Response<List<PolicyDTO>> getStorePolicies(String sessionToken, String storeId) {
        return policyService.getAllStorePolicies(sessionToken, storeId);
    }
    
}
 