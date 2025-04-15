package Domain.Store;

import java.util.List;

import Domain.Pair;

public class ItemFacade {
    private IItemRepository itemRepository;

    public ItemFacade(IItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    public List<Item> filterItems(ItemFilter itemFilter){
        List<Item> availableItems = itemRepository.getAvailabeItems();
        return availableItems.stream().filter(itemFilter::matchesFilter).toList();
    }

    public List<Item> getItemsProductId(String productId){
        return itemRepository.getByProductId(productId);
    }

    public Item getItem(String storeId, String productId){
        return itemRepository.getItem(storeId, productId);
    }

    public List<Item> getItemsByStoreId(String storeId){
        return itemRepository.getByStoreId(storeId);
    }

    
    public List<Item> getAvailableItems(){
        return itemRepository.getAvailabeItems();
    }

    public void update(Pair<String, String> id, Item item) {
        itemRepository.update(id, item);
    }
    
    public boolean add(Pair<String, String> id, Item item) {
        return itemRepository.add(id, item);
    }

    public Item remove(Pair<String, String> id) {
        return itemRepository.remove(id);
    }
}
