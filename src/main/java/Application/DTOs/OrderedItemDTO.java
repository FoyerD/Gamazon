package Application.DTOs;

import java.util.HashSet;
import java.util.Set;


import Domain.Store.Category;
import Domain.Store.Product;

public class OrderedItemDTO {
    private final String storeName;
    private final String name;
    private final Set<Category> categories;
    private final int quantity;
    private final double price;

    public OrderedItemDTO(String name, Set<Category> categories, String storeName, int quantity, double price) {
        this.storeName = storeName;
        this.name = name;
        this.categories = categories;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderedItemDTO(Product product, int quantity, String storeName, double price) {
        this.price = price;
        this.storeName = storeName;
        this.name = product.getName();
        this.categories = new HashSet<>(product.getCategories());
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

    public double getPrice() {
        return price;
    }

    public String toString() {
        return "OrderDTO{" +
                "storeName='" + storeName + '\'' +
                ", name='" + name + '\'' +
                ", categories=" + categories.stream().map(Category::toString).collect(java.util.stream.Collectors.toList()) +
                ", quantity=" + quantity +
                '}';
    }
}