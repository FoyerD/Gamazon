package Application.DTOs;

import java.util.Set;

import Domain.Store.Product;


public class ClientItemDTO {
    private final String clientName;
    private final String productName;
    private final Set<CategoryDTO> categories;
    private final int quantity;
    private final double price;

    public ClientItemDTO(String clientName, String productName, Set<CategoryDTO> categories, 
                         int quantity, double price) {
        this.clientName = clientName;
        this.productName = productName;
        this.categories = categories;
        this.quantity = quantity;
        this.price = price;
    }

    public ClientItemDTO(Product product, String clientName,  int quantity, double price) {
        this.clientName = clientName;
        this.productName = product.getName();
        this.categories = product.getCategories().stream()
                .map(CategoryDTO::fromCategory)
                .collect(java.util.stream.Collectors.toSet());
        this.quantity = quantity;
        this.price = price;
    }

    public String getClientName() {
        return clientName;
    }

    public String getProductName() {
        return productName;
    }

    public Set<CategoryDTO> getCategories() {
        return categories;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}
