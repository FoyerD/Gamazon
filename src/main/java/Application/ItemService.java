package Application;

import java.util.List;
import java.util.stream.Collectors;

import Application.DTOs.ItemDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Pair;
import Domain.Store.Item;
import Domain.Store.ItemFacade;
import Domain.Store.ItemFilter;

public class ItemService {

    private final ItemFacade itemFacade;

    public ItemService(ItemFacade itemFacade) {
        this.itemFacade = itemFacade;
    }

    public Response<Boolean> changePrice(String storeId, String productId, float newPrice) {
        String method = "changePrice";
        Response<ItemDTO> itemRes = getItem(storeId, productId);
        if (itemRes.errorOccurred()) {
            TradingLogger.logError("ItemService", method, itemRes.getErrorMessage());
            return new Response<>(new Error(itemRes.getErrorMessage()));
        }
        try {
            itemFacade.getItem(storeId, productId).setPrice(newPrice);
            TradingLogger.logEvent("ItemService", method, "Price changed successfully.");
            return new Response<>(true);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByProductId(String productId) {
        String method = "getItemsByProductId";
        try {
            List<ItemDTO> dtos = itemFacade.getItemsProductId(productId).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched items by productId: " + productId);
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> filterItems(ItemFilter filter) {
        String method = "filterItems";
        try {
            List<ItemDTO> dtos = itemFacade.filterItems(filter).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Items filtered.");
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> getItem(String storeId, String productId) {
        String method = "getItem";
        try {
            Item item = itemFacade.getItem(storeId, productId);
            TradingLogger.logEvent("ItemService", method, "Item retrieved successfully.");
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByStoreId(String storeId) {
        String method = "getItemsByStoreId";
        try {
            List<ItemDTO> dtos = itemFacade.getItemsByStoreId(storeId).stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched items for storeId: " + storeId);
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getAvailableItems() {
        String method = "getAvailableItems";
        try {
            List<ItemDTO> dtos = itemFacade.getAvailableItems().stream()
                .map(ItemDTO::fromItem).collect(Collectors.toList());
            TradingLogger.logEvent("ItemService", method, "Fetched available items.");
            return new Response<>(dtos);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> addRating(String storeId, String productId, float rating) {
        String method = "addRating";
        try {
            throw new UnsupportedOperationException("Not Implemented.");
        } catch (UnsupportedOperationException ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> add(Pair<String, String> id, Item item) {
        String method = "add";
        try {
            boolean added = itemFacade.add(id, item);
            TradingLogger.logEvent("ItemService", method, "Item added: " + added);
            return new Response<>(added);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> remove(Pair<String, String> id) {
        String method = "remove";
        try {
            Item item = itemFacade.remove(id);
            TradingLogger.logEvent("ItemService", method, "Item removed.");
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> increaseAmount(Pair<String, String> id, int amount) {
        String method = "increaseAmount";
        try {
            itemFacade.increaseAmount(id, amount);
            TradingLogger.logEvent("ItemService", method, "Amount increased.");
            return new Response<>(null);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> decreaseAmount(Pair<String, String> id, int amount) {
        String method = "decreaseAmount";
        try {
            itemFacade.decreaseAmount(id, amount);
            TradingLogger.logEvent("ItemService", method, "Amount decreased.");
            return new Response<>(null);
        } catch (Exception ex) {
            TradingLogger.logError("ItemService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
