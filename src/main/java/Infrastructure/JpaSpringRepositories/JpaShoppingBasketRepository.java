package Infrastructure.JpaSpringRepositories;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Shopping.ShoppingBasketId;

@Repository
@Profile("prod")
public class JpaShoppingBasketRepository extends IShoppingBasketRepository {

    private final IJpaShoppingBasketRepository jpaShoppingBasketRepository;

    public JpaShoppingBasketRepository(IJpaShoppingBasketRepository jpaShoppingBasketRepository) {
        this.jpaShoppingBasketRepository = jpaShoppingBasketRepository;
    }

    @Override
    public boolean add(Pair<String, String> id, ShoppingBasket value) {
        if (jpaShoppingBasketRepository.existsById(new ShoppingBasketId(id.getFirst(), id.getSecond())))
            return false;
        // Create a lock for the basket before saving
        addLock(id);
        jpaShoppingBasketRepository.save(value);
        return true;
    }

    @Override
    public ShoppingBasket remove(Pair<String, String> id) {
        ShoppingBasketId basketId = new ShoppingBasketId(id.getFirst(), id.getSecond());
        ShoppingBasket existing = jpaShoppingBasketRepository.findById(basketId).orElse(null);
        if (existing != null) {
            jpaShoppingBasketRepository.deleteById(basketId);
            removeLock(id);
        }
        return existing;
    }

    @Override
    public ShoppingBasket get(Pair<String, String> id) {
        return jpaShoppingBasketRepository.findById(
            new ShoppingBasketId(id.getFirst(), id.getSecond())
        ).orElse(null);
    }

    @Override
    public ShoppingBasket update(Pair<String, String> id, ShoppingBasket basket) {
        ShoppingBasketId basketId = new ShoppingBasketId(id.getFirst(), id.getSecond());
        if (!jpaShoppingBasketRepository.existsById(basketId)) {
            return null;
        }
        // Ensure a lock exists for the basket
        if (getLock(id) == null) {
            addLock(id);
        }
        return jpaShoppingBasketRepository.save(basket);
    }

    @Override
    public void clear() {
        jpaShoppingBasketRepository.deleteAll();
        deleteAllLocks();
    }

    @Override
    public void deleteAll() {
        jpaShoppingBasketRepository.deleteAll();
        deleteAllLocks();
    }
} 