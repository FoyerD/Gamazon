package Application;

import java.util.List;
import java.util.stream.Collectors;

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
        Response<ItemDTO> itemRes = getItem(storeId, productId);
        if (itemRes.errorOccurred())
            return new Response<>(new Error(itemRes.getErrorMessage()));
        try {
            itemFacade.getItem(storeId, productId).setPrice(newPrice);
            return new Response<>(true);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByProductId(String productId) {
        try {
            List<ItemDTO> dtos = itemFacade.getItemsProductId(productId)
                                           .stream()
                                           .map(ItemDTO::fromItem)
                                           .collect(Collectors.toList());
            return new Response<>(dtos);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> filterItems(ItemFilter filter) {
        try {
            List<ItemDTO> dtos = itemFacade.filterItems(filter)
                                           .stream()
                                           .map(ItemDTO::fromItem)
                                           .collect(Collectors.toList());
            return new Response<>(dtos);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> getItem(String storeId, String productId) {
        try {
            Item item = itemFacade.getItem(storeId, productId);
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getItemsByStoreId(String storeId) {
        try {
            List<ItemDTO> dtos = itemFacade.getItemsByStoreId(storeId)
                                           .stream()
                                           .map(ItemDTO::fromItem)
                                           .collect(Collectors.toList());
            return new Response<>(dtos);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<List<ItemDTO>> getAvailableItems() {
        try {
            List<ItemDTO> dtos = itemFacade.getAvailableItems()
                                           .stream()
                                           .map(ItemDTO::fromItem)
                                           .collect(Collectors.toList());
            return new Response<>(dtos);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> addRating(String storeId, String productId, float rating) {
        try {
            throw new UnsupportedOperationException("Not Implemented.");
        } catch (UnsupportedOperationException ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Boolean> add(Pair<String, String> id, Item item) {
        try {
            return new Response<>(itemFacade.add(id, item));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<ItemDTO> remove(Pair<String, String> id) {
        try {
            Item item = itemFacade.remove(id);
            return new Response<>(ItemDTO.fromItem(item));
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> increaseAmount(Pair<String, String> id, int amount) {
        try {
            itemFacade.increaseAmount(id, amount);
            return new Response<>(null);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    public Response<Void> decreaseAmount(Pair<String, String> id, int amount) {
        try {
            itemFacade.decreaseAmount(id, amount);
            return new Response<>(null);
        } catch (Exception ex) {
            return new Response<>(new Error(ex.getMessage()));
        }
    }
}
