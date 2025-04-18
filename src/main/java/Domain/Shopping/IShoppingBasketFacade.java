package Domain.Shopping;

// In question if even needed

public interface IShoppingBasketFacade {
    void decreaseInventory(String productId, int quantity);
    void addInventory(String productId, int quantity);
    boolean hasSufficientInventory(String productId, int quantity);
}