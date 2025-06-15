package Domain.Shopping;

import java.util.Set;

import Domain.Store.Category;

public class OrderedItem {
    private final String name;
    private final String description;
    private final Set<Category> categories;
    private final String storeName;
    private final int quantity;
    private final double price;
    
    public OrderedItem(String name, String description, Set<Category> categories, String storeName, int quantity, double price) {
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.storeName = storeName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }
    public Set<Category> getCategories() {
        return categories;
    }
    public String getStoreName() {
        return storeName;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getPrice() {
        return price;
    }
    public String getDescription() {
        return description;
    }

}
