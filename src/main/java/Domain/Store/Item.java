package Domain.Store;


public class Item {

    private String storeId;
    private String productId;
    private float price;
    private int amount;
    private String description;
    private float rating;


    public Item(String storeId, String productId, float price, int amount, float rating,  String description) {
        this.storeId = storeId;
        this.productId = productId;
        this.price = price;
        this.amount = amount;
        this.description = description;
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
