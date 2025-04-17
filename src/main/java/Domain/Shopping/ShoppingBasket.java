package Domain.Shopping;

public class ShoppingBasket implements IShoppingBasket {
    
    private String clientId;
    private String storeId;
    private Map<String, Integer> products;

    public ShoppingBasket(String clientId, String storeId) {
        this.clientId = clientId;
        this.storeId = storeId;
        this.products = new HashMap<>();
    }

    public String getClientId() {
        return clientId;
    }

    public String getStoreId() {
        return storeId;
    }

    public Map<String, Integer> getProducts() {
        return products.clone();
    }

    public void addItem(String productId, int quantity) {
        if (products.containsKey(productId)) {
            products.put(productId, products.get(productId) + quantity);
        } else {
            products.put(productId, quantity);
        }
    }

    public void removeItem(String productId, int quantity) {
        products.put(productId, products.get(productId) - quantity);
        if (products.get(productId) <= 0) {
            products.remove(productId);
        }
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public void clear() {
        products.clear();
    }

    
    // Returns the quantity of a specific product in the basket
    // If the product is not in the basket, it returns 0
    @Override
    public int getProduct(String productId) {
        return products.getOrDefault(productId, 0);
    }


}
