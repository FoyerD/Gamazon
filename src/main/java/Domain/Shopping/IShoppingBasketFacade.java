public interface IShoppingBasketFacade {
    boolean hasSufficientInventory(String productId, int quantity);
    void decreaseInventory(String productId, int quantity);
    void addInventory(String productId, int quantity);
}