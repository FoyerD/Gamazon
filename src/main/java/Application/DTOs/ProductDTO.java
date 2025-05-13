package Application.DTOs;

import java.util.HashSet;
import java.util.Set;

import Domain.Store.Category;
import Domain.Store.Product;

public class ProductDTO {
    private String id;
    private String name;
    private Set<CategoryDTO> categories;

    public ProductDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductDTO(String id, String name, Set<CategoryDTO> categories) {
        this.id = id;
        this.name = name;
        this.categories = categories;
    }

    public ProductDTO(Product product) {
        this.id = product.getProductId();
        this.name = product.getName();
        this.categories = new HashSet<>();
        for (Category category : product.getCategories()) {
            this.categories.add(new CategoryDTO(category));
        }
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Set<CategoryDTO> getCategories() {
        return categories;
    }
    
}
