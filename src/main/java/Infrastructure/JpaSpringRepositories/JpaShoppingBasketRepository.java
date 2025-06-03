package Infrastructure.JpaSpringRepositories;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import Domain.Pair;
import Domain.Repos.IShoppingBasketRepository;
import Domain.Repos.IUserRepository;
import Domain.Shopping.ShoppingBasket;
import Domain.Shopping.ShoppingBasketId;

import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("prod")
public class JpaShoppingBasketRepository extends IShoppingBasketRepository {

    private final IJpaShoppingBasketRepository jpaShoppingBasketRepository;
    private final IUserRepository userRepository;
    private final ConcurrentHashMap<Pair<String, String>, ShoppingBasket> guestBasketMemory = new ConcurrentHashMap<>();

    public JpaShoppingBasketRepository(IJpaShoppingBasketRepository jpaShoppingBasketRepository, IUserRepository userRepository) {
        this.jpaShoppingBasketRepository = jpaShoppingBasketRepository;
        this.userRepository = userRepository;
    }

    private boolean isGuestUser(String clientId) {
        return userRepository.getGuest(clientId) != null;
    }

    @Override
    public boolean add(Pair<String, String> id, ShoppingBasket value) {
        if (isGuestUser(id.getFirst())) {
            addLock(id);
            guestBasketMemory.put(id, value);
            return true;
        }

        if (jpaShoppingBasketRepository.existsById(new ShoppingBasketId(id.getFirst(), id.getSecond()))) {
            return false;
        }

        addLock(id);
        jpaShoppingBasketRepository.save(value);
        return true;
    }

    @Override
    public ShoppingBasket get(Pair<String, String> id) {
        if (isGuestUser(id.getFirst())) {
            return guestBasketMemory.get(id);
        }

        return jpaShoppingBasketRepository.findById(
            new ShoppingBasketId(id.getFirst(), id.getSecond())
        ).orElse(null);
    }

    @Override
    public ShoppingBasket update(Pair<String, String> id, ShoppingBasket basket) {
        if (isGuestUser(id.getFirst())) {
            if (getLock(id) == null) {
                addLock(id);
            }
            guestBasketMemory.put(id, basket);
            return basket;
        }

        ShoppingBasketId basketId = new ShoppingBasketId(id.getFirst(), id.getSecond());
        if (!jpaShoppingBasketRepository.existsById(basketId)) {
            return null;
        }

        if (getLock(id) == null) {
            addLock(id);
        }

        return jpaShoppingBasketRepository.save(basket);
    }

    @Override
    public ShoppingBasket remove(Pair<String, String> id) {
        if (isGuestUser(id.getFirst())) {
            removeLock(id);
            return guestBasketMemory.remove(id);
        }

        ShoppingBasketId basketId = new ShoppingBasketId(id.getFirst(), id.getSecond());
        ShoppingBasket existing = jpaShoppingBasketRepository.findById(basketId).orElse(null);
        if (existing != null) {
            jpaShoppingBasketRepository.deleteById(basketId);
            removeLock(id);
        }

        return existing;
    }

    @Override
    public void clear() {
        jpaShoppingBasketRepository.deleteAll();
        deleteAllLocks();
        guestBasketMemory.clear();
    }

    @Override
    public void deleteAll() {
        jpaShoppingBasketRepository.deleteAll();
        deleteAllLocks();
        guestBasketMemory.clear();
    }
}
