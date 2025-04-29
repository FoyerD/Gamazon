package Domain.Store;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import Domain.Pair;

public class ItemFacade {
    private final IItemRepository itemRepository;
    private final IProductRepository productRepository;
    private final IStoreRepository storeRepository;

    public ItemFacade(IItemRepository itemRepository, IProductRepository productRepository, IStoreRepository storeRepository){
        this.itemRepository = itemRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    public List<Item> filterItems(ItemFilter itemFilter){
        List<Item> availableItems = itemRepository.getAvailabeItems();
        return availableItems.stream().filter(itemFilter::matchesFilter).toList();
    }

    public List<Item> getItemsProductId(String productId){
        return itemRepository.getByProductId(productId);
    }

    public Item getItem(String storeId, String productId){
        validateStoreAndProductExist(storeId, productId);
        Item item = itemRepository.getItem(storeId, productId);
        if (item == null)
            throw new NoSuchElementException("Item not found for storeId: " + storeId + ", productId: " + productId);
        return item;
    }

    public List<Item> getItemsByStoreId(String storeId){
        if (storeRepository.get(storeId) == null)
            throw new NoSuchElementException("Store not found for storeId: " + storeId);
        return itemRepository.getByStoreId(storeId);
    }

    public List<Item> getAvailableItems(){
        return itemRepository.getAvailabeItems();
    }

    public void update(Pair<String, String> id, Item item) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        itemRepository.update(id, item);
    }

    public boolean add(Pair<String, String> id, Item item) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());

        if (itemRepository.get(id) != null) {
            return false; // Item already exists
        }

        item.setCategoryFetcher(() ->
            itemRepository.getByProductId(item.getProductId()).stream()
                .flatMap(i -> i.getCategories().stream())
                .collect(Collectors.toSet())
        );

        item.setNameFetcher(() ->
            itemRepository.getByProductId(item.getProductId()).stream()
                .findFirst()
                .map(i -> {
                    i.setNameFetcher(() -> ""); 
                    return i.getProductName();
                })
                .orElse("Unknown Product")
        );
        return itemRepository.add(id, item);
    }

    public Item remove(Pair<String, String> id) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        Item item = itemRepository.remove(id);
        if (item == null)
            throw new NoSuchElementException("No item with id: " + id.toString() + " exists.");
        return item;
    }

    public void increaseAmount(Pair<String, String> id, int amount) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        itemRepository.get(id).increaseAmount(amount);
    }

    public void decreaseAmount(Pair<String, String> id, int amount) {
        validateStoreAndProductExist(id.getFirst(), id.getSecond());
        itemRepository.get(id).decreaseAmount(amount);
    }

    private void validateStoreAndProductExist(String storeId, String productId) {
        if (storeRepository.get(storeId) == null) {
            throw new NoSuchElementException("Store not found for storeId: " + storeId);
        }
        if (productRepository.get(productId) == null) {
            throw new NoSuchElementException("Product not found for productId: " + productId);
        }
    }
}
