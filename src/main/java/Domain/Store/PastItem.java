package Domain.Store;

public class PastItem {
    private String storeId;
    private String productId;
    private float price;
    private int amount;
    private String description;


    public PastItem(String storeId, String productId, float price, int amount, String description) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
    }
    public PastItem(Item item) {
        this.storeId = item.getStoreId();
        this.productId = item.getProductId();
        this.price = item.getPrice();
        this.amount = item.getAmount();
        this.description = item.getDescription();
    }

    public String getStoreId() {
        return storeId;
    }

    public String getProductId() {
        return productId;
    }

    public int getAmount() {
        return amount;
    }

    public float getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }
    
    public void setAmount(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }
}
