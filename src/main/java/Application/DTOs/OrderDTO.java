package Application.DTOs;

import java.util.HashSet;
import java.util.Set;

import Domain.Store.Category;
import Domain.Store.Item;

public class OrderDTO {
    private String storeName;
    private String name;
    private String description;
    private Set<Category> categories;
    private int quantity;

    public OrderDTO() {
    }

    public OrderDTO(String name, String description, Set<Category> categories, String storeName, int quantity) {
        this.storeName = storeName;
        this.name = name;
        this.description = description;
        this.categories = categories;
        this.quantity = quantity;
    }

    public OrderDTO(Item item, int quantity, String storeName) {
        this.storeName = storeName;
        this.name = item.getProductName();
        this.description = item.getDescription();
        this.categories = new HashSet<>(item.getCategories());
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public Set<Category> getCategories() {
        return new HashSet<>(categories);
    }

    public String getStoreName() {
        return storeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String toString() {
        return "OrderDTO{" +
                "storeName='" + storeName + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", categories=" + categories.stream().map(Category::toString).collect(java.util.stream.Collectors.toList()) +
                ", quantity=" + quantity +
                '}';
    }
}