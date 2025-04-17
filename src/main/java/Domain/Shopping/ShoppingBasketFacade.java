package Domain.Shopping;

class ShoppingBasketFacade implements IShoppingBasketFacade {
    private final IShoppingBasketRepository basketRepo;

    public ShoppingBasketFacade(IShoppingBasketRepository basketRepo) {
        this.basketRepo = basketRepo;
    }

    @Override
    public void decreaseInventory(String productId, int quantity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void addInventory(String productId, int quantity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
