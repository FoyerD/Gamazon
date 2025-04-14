public class ShoppingBasketFacade implements IShoppingBasketFacade {
    private final IShoppingBasketRepository basketRepo;

    public ShoppingBasketFacade(IShoppingBasketRepository basketRepo) {
        this.basketRepo = basketRepo;
    }

    @Override
    public boolean hasSufficientInventory(String productId, int quantity) {
        return basketRepo.getInventory(productId) >= quantity;
    }

    @Override
    public void decreaseInventory(String productId, int quantity) {
        int stock = basketRepo.getInventory(productId);
        if (stock < quantity) {
            throw new RuntimeException("Insufficient inventory");
        }
        basketRepo.updateInventory(productId, stock - quantity);
    }

    @Override
    public void addInventory(String productId, int quantity) {
        int stock = basketRepo.getInventory(productId);
        basketRepo.updateInventory(productId, stock + quantity);
    }
}
