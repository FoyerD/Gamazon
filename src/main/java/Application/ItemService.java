package Application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import Application.DTOs.ItemDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Pair;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.ItemFilter;
import Domain.management.PermissionManager;
import Domain.management.PermissionType;

@Service
public class ItemService {

    private final ItemFacade itemFacade;
    private TokenService tokenService;
    private PermissionManager permissionManager;

    public ItemService(ItemFacade itemFacade, TokenService tokenService, PermissionManager permissionManager) {
        this.tokenService = tokenService;
        this.itemFacade = itemFacade;
        this.permissionManager = permissionManager;
    }

    public Response<Boolean> changePrice(String sessionToken, String storeId, String productId, float newPrice) {
        String method = "changePrice";
        Response<ItemDTO> itemRes = getItem(sessionToken, storeId, productId);
        if (itemRes.errorOccurred()) {
            TradingLogger.logError("ItemService", method, itemRes.getErrorMessage());
            return new Response<>(new Error(itemRes.getErrorMessage()));
        }
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, storeId, PermissionType.HANDLE_INVENTORY);
            itemFacade.getItem(storeId, productId).setPrice(newPrice);
            TradingLogger.logEvent("ItemService", method, "Price changed successfully.");
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByProductId(String sessionToken, String productId) {
        String method = "getItemsByProductId";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            List<ItemDTO> dtos = itemFacade.getItemsProductId(productId).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched items by productId: " + productId);
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> filterItems(String sessionToken, ItemFilter filter) {
        String method = "filterItems";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            List<ItemDTO> dtos = itemFacade.filterItems(filter).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Items filtered.");
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> getItem(String sessionToken, String storeId, String productId) {
        String method = "getItem";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            Item item = itemFacade.getItem(storeId, productId);
            TradingLogger.logEvent("ItemService", method, "Item retrieved successfully.");
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByStoreId(String sessionToken, String storeId) {
        String method = "getItemsByStoreId";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            List<ItemDTO> dtos = itemFacade.getItemsByStoreId(storeId).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched items for storeId: " + storeId);
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getAvailableItems(String sessionToken) {
        String method = "getAvailableItems";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            List<ItemDTO> dtos = itemFacade.getAvailableItems().stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched available items.");
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> addRating(String sessionToken, String storeId, String productId, int rating) {
        String method = "addRating";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            itemFacade.addRating(storeId, productId, rating);
            return new Response<>(null);
        } catch (UnsupportedOperationException ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }


    public Response<ItemDTO> add(String sessionToken, String storeId, String productId, String description) {
        return this.add(sessionToken, storeId, productId, 0, 0, description);
    }
    public Response<ItemDTO> add(String sessionToken, String storeId, String productId, double price, int amount, String description) {
        String method = "add";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, storeId, PermissionType.HANDLE_INVENTORY);
            Item item = itemFacade.add(storeId, productId, price, amount, description);
            if(item == null) {
                throw new RuntimeException("Item not added");
            }
            
            TradingLogger.logEvent("ItemService", method, "Item added: " + storeId + ", " + productId);
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> remove(String sessionToken, Pair<String, String> id) {
        String method = "remove";
        try {
            TradingLogger.logEvent("ItemService", method, "ATTEMPTING TO REMOVE ITEM");
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, id.getFirst(), PermissionType.HANDLE_INVENTORY);
            Item item = itemFacade.remove(id);
            TradingLogger.logEvent("ItemService", method, "Item removed.");
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> increaseAmount(String sessionToken, Pair<String, String> id, int amount) {
        String method = "increaseAmount";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, id.getFirst(), PermissionType.HANDLE_INVENTORY);
            itemFacade.increaseAmount(id, amount);
            TradingLogger.logEvent("ItemService", method, "Amount increased.");
            return new Response<>(null);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> decreaseAmount(String sessionToken, Pair<String, String> id, int amount) {
        String method = "decreaseAmount";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            permissionManager.checkPermission(userId, id.getFirst(), PermissionType.HANDLE_INVENTORY);
            itemFacade.decreaseAmount(id, amount);
            TradingLogger.logEvent("ItemService", method, "Amount decreased.");
            return new Response<>(null);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
