package Application;

import java.util.HashSet;
import java.util.Set;

import Domain.Store.Category;
import Domain.Store.Item;

public class OrderDTO {
    private String storeId;
    private String productId;
    private String name;
    private String description;
    private Set<Category> categories;
    private int quantity;

    public OrderDTO() {
    }

    public OrderDTO(String productId, String name, String description, Set<Category> categories, String storeId, int quantity) {
        this.storeId = storeId;
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.quantity = quantity;
    }

    public OrderDTO(Item item, int quantity) {
        this.storeId = item.getStoreId();
        this.productId = item.getProductId();
        this.name = item.getProductName();
        this.description = item.getDescription();
        this.categories = new HashSet<>(item.getCategories());
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public Set<Category> getCategories() {
        return new HashSet<>(categories);
    }

    public String getStoreId() {
        return storeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String toString() {
        return "OrderDTO{" +
                "storeId='" + storeId + '\'' +
                ", productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categories=" + categories.stream().map(Category::toString).collect(java.util.stream.Collectors.toList()) +
                ", quantity=" + quantity +
                '}';
    }
}