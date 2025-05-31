package Infrastructure.JpaSpringRepositories;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IItemRepository;
import Domain.Store.Item;
import Domain.Store.ItemId;

@Repository
@Profile("prod")
public class JpaItemRepository extends IItemRepository {

    private final IJpaItemRepository jpaItemRepository;

    public JpaItemRepository(IJpaItemRepository jpaItemRepository) {
        this.jpaItemRepository = jpaItemRepository;
    }

    @Override
    public boolean add(Pair<String, String> id, Item value) {
        if (jpaItemRepository.existsById(new ItemId(id.getFirst(), id.getSecond())))
            return false;
        // Create a lock for the item before saving
        addLock(id);
        jpaItemRepository.save(value);
        return true;
    }

    @Override
    public Item remove(Pair<String, String> id) {
        ItemId itemId = new ItemId(id.getFirst(), id.getSecond());
        Item existing = jpaItemRepository.findById(itemId).orElse(null);
        if (existing != null) {
            jpaItemRepository.deleteById(itemId);
            removeLock(id);
        }
        return existing;
    }

    @Override
    public Item get(Pair<String, String> id) {
        return jpaItemRepository.findById(new ItemId(id.getFirst(), id.getSecond())).orElse(null);
    }

    @Override
    public Item getItem(String storeId, String productId) {
        return jpaItemRepository.getItem(storeId, productId);
    }

    @Override
    public List<Item> getByStoreId(String storeId) {
        return jpaItemRepository.getByStoreId(storeId);
    }

    @Override
    public List<Item> getByProductId(String productId) {
        return jpaItemRepository.getByProductId(productId);
    }

    @Override
    public List<Item> getAvailabeItems() {
        return jpaItemRepository.getAvailabeItems();
    }

    @Override
    public Item update(Pair<String, String> id, Item item) {
        // Upsert logic: update only if it exists
        ItemId itemId = new ItemId(id.getFirst(), id.getSecond());
        if (!jpaItemRepository.existsById(itemId)) {
            return null;
        }
        // Ensure a lock exists for the item
        if (getLock(id) == null) {
            addLock(id);
        }
        return jpaItemRepository.save(item);
    }

    @Override
    public void deleteAll() {
        jpaItemRepository.deleteAll();
        deleteAllLocks();
    }
}
